import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.ui.Log;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

public class combatGUI extends JFrame {

    public String mobName;

    public combatGUI ()
    {
        super("Combat Configuration");
        setLayout(new FlowLayout());

        Npc npcs[] = Npcs.getLoaded(x -> x.getCombatLevel() > 0);

        HashSet<String> npcNames = new HashSet<>();

        for (Npc npc: npcs) {
            npcNames.add(npc.getName());
        }

        if(npcs == null)
            return;

        JComboBox npcCombo = new JComboBox(npcNames.toArray());
        JCheckBox specialCheck = new JCheckBox("Special");
        JCheckBox potionCheck = new JCheckBox("Potion");

        JButton  start = new JButton("Start");

        start.addActionListener(e -> {
            combat.mobName = npcCombo.getSelectedItem().toString();
            combat.specialAttack = specialCheck.isSelected();
            combat.usePotions = potionCheck.isSelected();
            Log.info("OPTIONS LOADED");
            setVisible(false);
        });

        add(npcCombo);
        add(specialCheck);
        add(potionCheck);
        add(start);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        pack();
    }



    public static void main(String... args)
    {
        new combatGUI().setVisible(true);
    }
}
