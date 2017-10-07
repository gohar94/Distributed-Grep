sudo yum -y install java-devel
wget http://apache.mesi.com.ar/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
tar xvf apache-maven-3.3.9-bin.tar.gz
sudo mv apache-maven-3.3.9 /usr/local/apache-maven
echo "export M2_HOME=/usr/local/apache-maven" >> ~/.bashrc
echo "export M2=\$M2_HOME/bin" >> ~/.bashrc
echo "export PATH=\$M2:\$PATH" >> ~/.bashrc
source ~/.bashrc
mvn -version
rm apache-maven-3.3.9-bin.tar.gz