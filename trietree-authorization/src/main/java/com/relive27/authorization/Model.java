package com.relive27.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于TireTree的模型
 *
 * @author: ReLive27
 * @date: 2023/3/7 19:14
 */
@Slf4j
public class Model {
    private static final String URL_PATH_SPLIT = "/";
    private static Pattern pattern = Pattern.compile("\\{[A-Za-z0-9]+\\}");
    private static Pattern PATH_SPLIT_PATTERN = Pattern.compile("/+");
    private volatile Node root = new Node();

    /**
     * 加载模型
     *
     * @param rules eg: /api/user/path==GET==ROLE1,ROLE2
     */
    public void loadModel(List<String> rules) {
        resetModel();
        for (String rule : rules) {
            addPath(this.root, rule);
        }
    }

    /**
     * 重新加载模型
     *
     * @param rules eg: /api/user/path==GET==ROLE1,ROLE2
     */
    public void reloadModel(List<String> rules) {
        Node buildRoot = new Node();
        for (String rule : rules) {
            addPath(buildRoot, rule);
        }
        this.root = buildRoot;
    }

    /**
     * 添加指定策略
     *
     * @param rules
     */
    public void addModel(List<String> rules) {
        for (String rule : rules) {
            addPath(this.root, rule);
        }
    }

    /**
     * 重制模型
     */
    public void resetModel() {
        this.root.children.clear();
    }

    /**
     * 删除指定策略
     *
     * @param rules
     */
    public void removeModel(List<String> rules) {
        for (String rule : rules) {
            removePath(this.root, rule);
        }
    }

    private void removePath(Node currentNode, String path) {
        if (!StringUtils.hasText(path) || !path.contains("==")) {
            return;
        }

        String[] pathSplit = path.split("==");
        if (pathSplit.length != 3) {
            return;
        }

        String url = pathSplit[0].substring(1); // Remove leading '/'
        String httpMethod = pathSplit[1].toLowerCase();
        String[] urlSegments = url.split(URL_PATH_SPLIT);

        removeNode(currentNode, urlSegments, httpMethod, 0);
    }

    private boolean removeNode(Node node, String[] urlSegments, String httpMethod, int index) {
        if (node == null) {
            return false;
        }

        if (index == urlSegments.length) {
            // We are at the leaf node where HTTP method is stored
            if (node.children.containsKey(httpMethod)) {
                node.children.remove(httpMethod);
                return node.children.isEmpty(); // If no other children exist, remove this node as well
            }
            return false;
        }

        String segment = urlSegments[index];
        if (node.children.containsKey(segment)) {
            boolean shouldRemove = removeNode(node.children.get(segment), urlSegments, httpMethod, index + 1);
            if (shouldRemove) {
                node.children.remove(segment);
                return node.children.isEmpty(); // Propagate removal upward if no children remain
            }
        }

        return false;
    }


    /**
     * 匹配角色权限
     *
     * @param path eg: /api/path==GET
     * @return
     */
    @Nullable
    public String findMatch(String path) {
        String[] pathSplit = parsePath(path);
        String[] urlSegments = pathSplit[0].substring(1).split("/");
        String httpMethod = pathSplit[1].toLowerCase();
        Node currentNode = this.root;
        return this.searchPath(currentNode, urlSegments, httpMethod, 0);
    }

    private String[] parsePath(String path) {
        if (!StringUtils.hasText(path) || !path.contains("==")) {
            throw new IllegalArgumentException("Invalid path format: " + path);
        }
        if (!path.startsWith(URL_PATH_SPLIT)) {
            path = URL_PATH_SPLIT.concat(path);
        }
        path = PATH_SPLIT_PATTERN.matcher(path).replaceAll("/");
        String[] parts = path.split("==");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Path must be in format: URL==METHOD");
        }

        return parts;
    }


    private String searchPath(Node node, String[] urlSegments, String httpMethod, int index) {
        if (node == null) {
            return null;
        }
        if (urlSegments.length == index) {
            Node methodNode = node.children.get(httpMethod);
            return methodNode != null ? methodNode.roles : null;
        }
        if (node.children.containsKey(urlSegments[index])) {
            return this.searchPath(node.children.get(urlSegments[index]), urlSegments, httpMethod, index + 1);
        } else if (node.children.containsKey("*")) {
            return this.searchPath(node.children.get("*"), urlSegments, httpMethod, index + 1);
        } else if (node.children.containsKey("**")) {
            return this.searchPath(node.children.get("**"), urlSegments, httpMethod, urlSegments.length);
        } else {
            return null;
        }
    }

    /**
     * 添加权限路径
     *
     * @param root
     * @param path eg: /api/user/path==GET==ROLE1,ROLE2
     */
    private void addPath(Node root, String path) {
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

    /**
     * 打印模型
     */
    public void printModel() {
        printNode(this.root, "");
    }

    private void printNode(Node node, String prefix) {
        if (node == null) {
            return;
        }
        System.out.println(prefix + (node.roles == null ? "[]" : node.roles));
        for (Map.Entry<String, Node> entry : node.children.entrySet()) {
            printNode(entry.getValue(), prefix + "  " + entry.getKey());
        }
    }

    static class Node {
        private String roles;
        private Map<String, Node> children;

        public Node() {
            this.children = new HashMap<>();
        }

        public Node(String roles) {
            this();
            this.roles = roles;
        }

    }
}
