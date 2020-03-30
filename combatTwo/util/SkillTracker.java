package util;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;

public class SkillTracker {

    private final EnumMap<Skill, Tracker> mapTracker;

    public SkillTracker() {
        this.mapTracker = new EnumMap<>(Skill.class);
    }

    public void check() {
        getGainedSkills().forEach(skill -> {
            if (getCurrentXP(skill) > mapTracker.get(skill).lastCheckedXP) {
                mapTracker.get(skill).onEXP();
            }
        });
    }

    public EnumMap<Skill, Tracker> getTrackerMap() {
        return this.mapTracker;
    }

    public long getTotalXpGained() {
        long amount = 0;
        for (Skill skill : getGainedSkills()) {
            amount += getGainedXP(skill);
        }
        return amount;
    }

    public List<Skill> getTrackedSkills() {
        return mapTracker.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<Skill> getGainedSkills() {
        return getTrackedSkills().stream().filter(this::hasGainedXP).collect(Collectors.toList());
    }

    private boolean hasGainedXP(Skill skill) {
        return getGainedXP(skill) > 0;
    }

    public void track(Skill... skills) {
        for (Skill skill : (skills.length == 0) ? Skill.values() : skills) {
            mapTracker.put(skill, new Tracker(skill));
        }
    }

    public Tracker get(Skill skill) {
        Tracker tracker = mapTracker.get(skill);
        if (tracker == null) {
            throw new UnsupportedOperationException("You're not tracking this skill!");
        }
        return tracker;
    }

    public long getElapsed(Skill skill) {
        return System.currentTimeMillis() - get(skill).startedTrackingAt;
    }

    public int getGainedLevels(Skill skill) {
        return Skills.getLevel(skill) - get(skill).getStartLevel();
    }

    public int getCurrentLevel(Skill skill) {
        return Skills.getLevel(skill);
    }

    public int getCurrentXP(Skill skill) {
        return Skills.getExperience(skill);
    }

    public int getGainedXP(Skill skill) {
        return getCurrentXP(skill) - get(skill).getStartExp();
    }

    public int getGainedXPPerHour(Skill skill) {
        if (get(skill).cachedXPGained != getGainedXP(skill)) {
            get(skill).cachedXPGained = getGainedXP(skill);
            get(skill).cachedXPHour = (int) (getGainedXP(skill)
                    / ((System.currentTimeMillis() - get(skill).startedTrackingAt) / 3600000.0D));
        }
        return get(skill).cachedXPHour;

    }

    public double percentToLevel(Skill skill) {
        int currentLevel = Skills.getLevel(skill);
        double expBetween = expBetween(currentLevel, currentLevel + 1);
        double expIntoLevel = Skills.getExperience(skill) - getExpForLevel(currentLevel);
        return ((expIntoLevel / expBetween) * 100D);
    }

    public long getTimeToLevel(Skill skill) {
        if (getGainedXP(skill) <= 0) {
            return 0;
        }
        return (long) ((expToLevel(skill) * 3600000.0D) / (double) getGainedXPPerHour(skill));
    }

    public long getTimeToLevel(Skill skill, int target) {
        if (getGainedXP(skill) <= 0) {
            return 0;
        }
        return (long) ((expToLevel(skill, target) * 3600000.0D) / (double) getGainedXPPerHour(skill));
    }

    public double getPercentTillNextLevel(Skill skill) {
        if (Skills.getLevel(skill) != 99) {
            int currentXP = Skills.getExperience(skill);
            int currentLevel = Skills.getLevel(skill);
            double currentLevelXP = Skills.getExperienceAt(currentLevel);
            double nextLevelXP = Skills.getExperienceAt(currentLevel + 1);

            return ((currentXP - currentLevelXP) / (nextLevelXP - currentLevelXP) * 100);
        } else {

            return 100;
        }
    }

    public int expToLevel(Skill skill) {
        return Skills.getExperienceToNextLevel(skill);
    }

    public int expToLevel(Skill skill, int target) {
        return getExpForLevel(target) - Skills.getExperience(skill);
    }

    public int expBetween(int value1, int value2) {
        return getExpForLevel(Math.max(value1, value2)) - getExpForLevel(Math.min(value1, value2));
    }

    public int actionsPerHour(long actions, long startTime) {
        return (int) (3600000D / (System.currentTimeMillis() - startTime) * actions);
    }

    public int getExpForLevel(int level) {
        return Skills.getExperienceAt(level);
    }

    public class Tracker {

        private final Skill skill;
        private final int startExp, startLevel;
        private boolean gained;
        private long startedTrackingAt;
        private int expGained, expPerHour, expTillLevel;
        private double percentTillNextLevel;
        private long timeTillLevel;
        private long lastCheckedXP;

        private int cachedXPGained, cachedXPHour = 0;

        private Tracker(final Skill track) {
            this.skill = track;
            this.startExp = Skills.getExperience(track);
            this.startLevel = Skills.getLevel(track);
            this.startedTrackingAt = System.currentTimeMillis();
            this.gained = false;
        }

        public int getStartExp() {
            return startExp;
        }

        public int getStartLevel() {
            return startLevel;
        }

        public long getStartedTrackingAt() {
            return startedTrackingAt;
        }

        public int getExpGained() {
            return expGained;
        }

        public int getExpPerHour() {
            return expPerHour;
        }

        public int getExpTillLevel() {
            return expTillLevel;
        }

        public long getTimeTillLevel() {
            return timeTillLevel;
        }

        public double percentTillNextLevel() {
            return percentTillNextLevel;
        }

        public void onEXP() {
            if (!gained) {
                this.startedTrackingAt = System.currentTimeMillis();
                this.gained = true;
            }
            this.expGained = getGainedXP(skill);
            this.expPerHour = getGainedXPPerHour(skill);
            this.percentTillNextLevel = getPercentTillNextLevel(skill);
            this.expTillLevel = getExpForLevel(getCurrentLevel(skill) + 1);
            this.timeTillLevel = getTimeToLevel(skill);
            this.lastCheckedXP = getCurrentXP(skill);
        }
    }
}

