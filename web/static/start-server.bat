@echo off
echo Starting MariaDB...
bin\mysqld --defaults-file=my-medium.ini --standalone --console
pause