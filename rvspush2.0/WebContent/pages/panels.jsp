<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">  
    <html>  
    <head>  
    <meta http-equiv="Content-Type" content="text/html; charset=GBK">  
    <title>Chart</title>  
<script type="text/javascript" src="http://localhost:8080/rvspush/js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript">  
    var ws = null;  
    function startWebSocket() {  
        if (!window.WebSocket) alert("WebSocket not supported by this browser!");  
        // 创建WebSocket  
        ws = new WebSocket("ws://localhost:8080/rvspush/position");  
        // 收到消息时在消息框内显示  
        ws.onmessage = function(evt) {
            $('#msgBox').append(evt.data);  
            $('#msgBox').append('</br>');  
        };  
        // 断开时会走这个方法  
        ws.onclose = function() {   
        };  
        // 连接上时走这个方法  
        ws.onopen = function() {     
            ws.send("dail:"+document.getElementById('msgName').value);
        };  
    }  
        
    // 发送消息  
    function sendMsg() {  
        var data = document.getElementById('msgSendBox').value;  
        ws.send(data);  
        document.getElementById('msgSendBox').value = '';  
    }

    </script>  
    </head>  
    <body>
    <input type="text" id="msgName" value="input it!"/>
    <input type="button" value="login" onclick="startWebSocket(); this.disabled=true">
    <div id="msgBox" style="width:400px;height:300px;border:1px solid #000000">  
    </div>  
    <textarea id="msgSendBox" rows="5" cols="32"></textarea>  
    <input type="button" value="send" onclick="sendMsg()"></input>  
    </body>  
    </html>  