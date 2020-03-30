package UI;

import extra.DefaultAPI;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.NPC;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AIOGUI extends JFrame{

    public static final int WIDTH = 500;
    public static final int HEIGHT = 500;

    private static DefaultAPI api;

    private JFrame frame;
    private List<NPC> npcs;
    private JButton btnStart;
    private JList listNPCs;
    private JPanel panelMain;
    private JTextField txtRadius;
    private JCheckBox chkFood;
    private JCheckBox chkPotions;
    private JCheckBox chkPrayer;
    private JCheckBox chkSpecial;

    private boolean settingsSaved;

    public AIOGUI(DefaultAPI api) {
        this.api = api;
        this.settingsSaved = false;

        btnStart.addActionListener(e -> {
            List listObjects = listNPCs.getSelectedValuesList();
            String[] npcStrings = new String[listObjects.size()];

            for (int i = 0; i < listObjects.size(); i++) {
                npcStrings[i] = listObjects.get(i).toString();
            }

            api.getExtraSettings().setNpcs(npcStrings);
            api.getExtraSettings().setFightRadius(Integer.parseInt(txtRadius.getText()));
            api.getExtraSettings().setUseFood(chkFood.isSelected());
            api.getExtraSettings().setUsePotions(chkPotions.isSelected());
            api.getExtraSettings().setUsePrayer(chkPrayer.isSelected());
            api.getExtraSettings().setUseSpecial(chkSpecial.isSelected());

            settingsSaved = true;

            frame.setVisible(false);
            frame.dispose();
        });
    }

    public void main(String[] args)
    {
        run();

        frame = new JFrame("AIOGUI");
        frame.setContentPane(this.panelMain);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.getContentPane().setSize(800,500);

    }

    public void run() {

        Set<String> npcNames = new HashSet<>();

        for(NPC npc: api.getNpcs().filter(new ActionFilter<>("Attack")))
        {
            npcNames.add(npc.getName());
        }

        listNPCs.setListData(npcNames.toArray());
    }

    public boolean getSettingsSaved()
    {
        return settingsSaved;
    }

}
