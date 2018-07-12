# JxTFTP - Java extra-TrivialFTP

This is an incomplete, yet functional TFTP server written in plain Java with optimization for code size and hopefully readability. As an academic exercise it demonstrates use of datagram sockets, non-blocking I/O and session multiplexing with single-threaded processing.

Server will respect GET and PUT requests, and will serve files from its current working directory. By default it will listen on port 6969.

```bash
xba1k@abuntu:~/jx/dist$ ls -al
total 36
drwxrwxr-x 2 xba1k xba1k  4096 Jul 12 07:20 .
drwxrwxr-x 7 xba1k xba1k  4096 Jul 12 07:27 ..
-rw-rw-r-- 1 xba1k xba1k 24633 Jul 12 07:19 JxTFTP.jar
xba1k@abuntu:~/jx/dist$ java -jar JxTFTP.jar 
Current directory: /home/xba1k/jx/dist
Creating new session for /127.0.0.1:46951
Removing expired session Session{address=/127.0.0.1:46951, filename=hello.txt, statBytesProcessed=0, blockNo=1, state=DONE, type=PUT, lastActivity=1531406157773}
Creating new session for /127.0.0.1:43307
Session Session{address=/127.0.0.1:43307, filename=hello.txt, statBytesProcessed=15, blockNo=1, state=DONE, type=GET, lastActivity=1531406171344} completed
Total bytes received: 65
Total bytes sent: 27
Sessions processed: 2
Active sessions:
Session{address=/127.0.0.1:43307, filename=hello.txt, statBytesProcessed=15, blockNo=1, state=DONE, type=GET, lastActivity=1531406171344}
Removing expired session Session{address=/127.0.0.1:43307, filename=hello.txt, statBytesProcessed=15, blockNo=1, state=DONE, type=GET, lastActivity=1531406171344}
^Cxba1k@abuntu:~/jx/dist$ ls -al
total 40
drwxrwxr-x 2 xba1k xba1k  4096 Jul 12 07:35 .
drwxrwxr-x 7 xba1k xba1k  4096 Jul 12 07:27 ..
-rw-rw-r-- 1 xba1k xba1k    15 Jul 12 07:35 hello.txt
-rw-rw-r-- 1 xba1k xba1k 24633 Jul 12 07:19 JxTFTP.jar
xba1k@abuntu:~/jx/dist$ 
xba1k@abuntu:~/jx/dist$ 


xba1k@abuntu:~$ tftp
tftp> connect localhost 6969
tftp> put hello.txt
Sent 15 bytes in 0.0 seconds
tftp> get hello.txt
Received 15 bytes in 0.0 seconds
tftp>
```
