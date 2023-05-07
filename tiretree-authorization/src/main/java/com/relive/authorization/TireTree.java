package com.relive.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: ReLive
 * @date: 2023/3/7 19:14
 */
@Slf4j
public class TireTree {
    private static final String URL_PATH_SPLIT = "/";
    private static Pattern pattern = Pattern.compile("\\{[A-Za-z0-9]+\\}");
    private static Pattern PATH_SPLIT_PATTERN = Pattern.compile("/+");
    private volatile Node root = new Node();

    public void buildTree(Set<String> paths) {
        clearTree();
        for (String path : paths) {
            insertNode(this.root, path);
        }
    }

    public void rebuildTree(Set<String> paths) {
        Node buildRoot = new Node();
        for (String path : paths) {
            insertNode(buildRoot, path);
        }
        this.root = buildRoot;
    }

    public void clearTree() {
        this.root.children.clear();
    }

    /**
     * @param path eg: /api/path==GET
     * @return
     */
    @Nullable
    public String searchPath(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        if (!path.startsWith(URL_PATH_SPLIT)) {
            path = URL_PATH_SPLIT.concat(path);
        }
        path = PATH_SPLIT_PATTERN.matcher(path).replaceAll("/");
        String[] pathSplit = path.split("==");
        if (pathSplit.length != 2) {
            return null;
        }
        String url = pathSplit[0].substring(1);
        String[] urlSplit = url.split("/");
        String httpMethod = pathSplit[1].toLowerCase();
        Node currentNode = this.root;
        Node node = this.searchPath(currentNode, urlSplit, httpMethod, 0);
        if (node == null) {
            return null;
        }
        return node.data;
    }

    private Node searchPath(Node node, String[] urlPath, String httpMethod, int flow) {
        if (node == null) {
            return null;
        }
        if (urlPath.length == flow) {
            return node.children.get(httpMethod);
        }
        if (node.children.containsKey(urlPath[flow])) {
            return this.searchPath(node.children.get(urlPath[flow]), urlPath, httpMethod, flow + 1);
        } else if (node.children.containsKey("*")) {
            return this.searchPath(node.children.get("*"), urlPath, httpMethod, flow + 1);
        } else if (node.children.containsKey("**")) {
            return this.searchPath(node.children.get("**"), urlPath, httpMethod, urlPath.length);
        } else {
            return null;
        }
    }

    /**
     * @param root
     * @param path eg: /api/user/path==GET==[ROLE1,ROLE2]
     */
    public void insertNode(Node root, String path) {
        if (!StringUtils.hasText(path)) {
            return;
        }
        if (!path.startsWith(URL_PATH_SPLIT)) {
            path = URL_PATH_SPLIT.concat(path);
        }
        String[] pathSplit = path.split("==");
        if (pathSplit.length != 3) {
            return;
        }
        String url = pathSplit[0].substring(1);
        String httpMethod = pathSplit[1].toLowerCase();
        String roles = pathSplit[2];
        Node currentNode = root;
        String[] urlSplit = url.split(URL_PATH_SPLIT);
        for (String urlPath : urlSplit) {
            if (this.patternPath(urlPath)) {
                urlPath = "*";
            }
            if (!currentNode.children.containsKey(urlPath)) {
                currentNode.children.put(urlPath, new Node());
            }
            currentNode = currentNode.children.get(urlPath);
        }
        currentNode.children.put(httpMethod, new Node(roles));
    }

    private boolean patternPath(String path) {
        Matcher matcher = pattern.matcher(path);
        return matcher.matches();
    }

    static class Node {
        private String data;
        private Map<String, Node> children;

        public Node() {
            this.children = new HashMap<>();
        }

        public Node(String data) {
            this();
            this.data = data;
        }
    }
}
