# two-level-proxy
用于代理内网，在主机没有公网IP时，可利用此工程将指定端口映射到公网上（需要一个云服务器）



外网代理参数
-pfo : port-for-outside  开放给用户的外网端口
-pfi : port-for-inside  用于给内网机器主动连接构建通道


内网代理参数
-ht : host-target 内网被代理终端的机器地址
-pt : port-target 内网被代理终端的机器端口

-hr : host-remote 外网主机地址
-pr : port-remote 外网主机端口，与外网参数的-pfi要一致
