

用户登录，验证用户名密码后重写登录成功处理器，如果启用MFA则响应mfa=true，是否首次登录，首次登录显示二维码，如果
不是首次登录则弹出输入框输入动态口令，然后请求认证，认证成功返回token。
如果不启用mfa，则直接生成token

动态口令和登录接口同一个接口要考虑第一次登录时没有输入口令要怎么判断


前端：
1.首先判断authenticated是否为true，如果为true则获取token。
2.其次判断mfa，如果是bind,