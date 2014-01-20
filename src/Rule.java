public class Rule
{
        private String action;
        private String source = null;
        private String destination = null;
        private String kind = null;
        private int Id = -1;
        private int EveryNth = -1;
        private int Nth = -1; 

        public Rule(String action) {
                this.action = action;
        }

        public void setAction(String action){this.action = action;}
        public void setSource(String source){this.source = source;}
        public void setDestination(String destination){this.destination = destination;}
        public void setKind(String kind){this.kind = kind;}
        public void setId(int Id){this.Id = Id;}
        public void setEveryNth(int EveryNth){this.EveryNth = EveryNth;}
        public void setNth(int Nth){this.Nth = Nth;}
        
        public String getAction(){return action;}
        public String getSource(){return source;}
        public String getDestination(){return destination;}
        public String getKind(){return kind;}
        public int getId(){return Id;}
        public int getEveryNth(){return EveryNth;}
        public int getNth(){return Nth;}
     
}