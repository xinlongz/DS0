public class User
{
        private String name;
        private String ip;
        private int port;

        public User(String Name, String ip, int port)
        {
                this.name = Name;
                this.ip = ip;
                this.port = port;
        }

        public String getName(){return name;}
        public String getIp(){return ip;}
        public int getPort(){return port;}

}