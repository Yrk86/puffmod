package com.puff.modules.impl;

import com.puff.core.Module;
import com.puff.core.settings.BooleanSetting;
import com.puff.core.settings.NumberSetting;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;

public class ChestStealer extends Module {

    private final NumberSetting delay = new NumberSetting("Delay", 100.0, 0.0, 500.0, 10.0);
    private final BooleanSetting autoClose = new BooleanSetting("Auto Close", true);
    
    private long lastClick = 0;

    public ChestStealer() {
        super("ChestStealer", Category.PLAYER);
        registerSetting(delay);
        registerSetting(autoClose);
    }

    @Override
    public void onEnable() {}
    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        if (mc.thePlayer == null || !(mc.thePlayer.openContainer instanceof ContainerChest)) {
            return;
        }

        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        
        // On récupère le nombre de slots du coffre (partie haute de l'inventaire ouvert)
        int chestSlots = chest.getLowerChestInventory().getSizeInventory();

        for (int i = 0; i < chestSlots; i++) {
            ItemStack stack = chest.getLowerChestInventory().getStackInSlot(i);
            
            if (stack != null && System.currentTimeMillis() - lastClick >= delay.getValue()) {
                // Shift-Click (QUICK_MOVE) pour envoyer l'item dans l'inventaire du joueur
                mc.playerController.windowClick(chest.windowId, i, 0, ClickType.QUICK_MOVE, mc.thePlayer);
                lastClick = System.currentTimeMillis();
                return; // Un item par tick (ou selon le délai) pour éviter les kicks
            }
        }

        // Si on arrive ici, le coffre est vide
        if (autoClose.getValue() && isChestEmpty(chest)) {
            mc.thePlayer.closeScreen();
        }
    }
    
    private boolean isChestEmpty(ContainerChest chest) {
        int chestSlots = chest.getLowerChestInventory().getSizeInventory();
        for (int i = 0; i < chestSlots; i++) {
            if (chest.getLowerChestInventory().getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }
}
