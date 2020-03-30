package combat;

import extra.NodeType;
import org.osbot.rs07.api.map.Area;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private Integer fightRadius;
    private Area fightArea;
    private Integer remainingKills;
    private String[] npcs;
    private NodeType activeNode = null;
    private boolean inCombat = false;
    private List<String> lootItems = new ArrayList<>();
    private boolean useFood = false;
    private boolean usePrayer = false;
    private boolean useSpecial = false;

    public Integer getRemainingKills()
    {
        if(remainingKills == null)
            return -1;

        return remainingKills;
    }

    public void setRemainingKills(Integer remainingKills) {
        this.remainingKills = remainingKills;
    }

    public void setNpcs(String[] npcNames)
    {
        this.npcs = npcNames;
    }

    public String[] getNpcs()
    {
        return  npcs;
    }

    public NodeType getActiveNode()
    {
        if(activeNode == null)
        {
            return NodeType.IDLE;
        }

        return activeNode;
    }

    public void setActiveNode(NodeType ln)
    {
        this.activeNode = ln;
    }

    public Area getFightArea() {
        return fightArea;
    }

    public void setFightArea(Area fightArea)
    {
        this.fightArea = fightArea;
    }

    public void setFightRadius(Integer fightRadius) {
        this.fightRadius = fightRadius;
    }

    public Integer getFightRadius() {
        return fightRadius;
    }

    public boolean isInCombat() {
        return inCombat;
    }

    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    public List<String> getLootItems() {
        return lootItems;
    }

    public void addLootItem(String item) {
        this.lootItems.add(item);
    }

    public void removeLootItem(String item)
    {
        this.lootItems.remove(item);
    }

    public boolean isUseFood() {
        return useFood;
    }

    public void setUseFood(boolean useFood) {
        this.useFood = useFood;
    }

    public boolean isUsePotions() {
        return usePotions;
    }

    public void setUsePotions(boolean usePotions) {
        this.usePotions = usePotions;
    }

    private boolean usePotions = false;

    public boolean isUsePrayer() {
        return usePrayer;
    }

    public void setUsePrayer(boolean usePrayer) {
        this.usePrayer = usePrayer;
    }

    public boolean isUseSpecial() {
        return useSpecial;
    }

    public void setUseSpecial(boolean useSpecial) {
        this.useSpecial = useSpecial;
    }
}
