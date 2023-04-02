## check version in apt repositories
# apt-cache policy maven
## install from apt
# sudo apt install maven

# java 8
sudo apt install openjdk-8-jdk

# install from # https://maven.apache.org/download.cgi

sudo mkdir /usr/local/apache-maven/ && cd "$_" || exit
sudo wget https://dlcdn.apache.org/maven/maven-3/3.9.1/binaries/apache-maven-3.9.1-bin.tar.gz
sudo tar xzvf apache-maven-*
ls -laFh

echo ' ' >>~/.bashrc
echo '# [vi] maven ---------- ' >>~/.bashrc

echo 'export JAVA_HOME=/home/vi/.jdks/openjdk-18.0.2.1' >>~/.bashrc
echo 'export PATH=$JAVA_HOME:$PATH' >>~/.bashrc

echo 'export M2_HOME=/usr/local/apache-maven/apache-maven-3.9.1' >>~/.bashrc
echo 'export M2=$M2_HOME/bin' >>~/.bashrc
echo 'export PATH=$M2:$PATH' >>~/.bashrc

source "$HOME"/.bashrc && mvn --version
cd - || exit
