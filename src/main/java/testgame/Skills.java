package testgame;

public class Skills {
    enum skills {
        SPEED_UP(0, "暴走达人", "加速"),
        BUILD_EXPERT(1, "建筑能手", "放置方块"),
        TROUBLE_MAKER(2, "障碍达人", "放置障碍");

        protected int id = 0;
        protected String jobName = null;
        protected String skillName = null;

        skills(int type, String jobName, String skillName) {
            this.id = type;
            this.jobName = jobName;
            this.skillName = skillName;
        }

        public String getSkillName() {
            return skillName;
        }

        public int getId() {
            return this.id;
        }

        public String getJobName() {
            return this.jobName;
        }
    }
}
