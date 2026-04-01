package mc.red.Gui.ClickGui.comp;

import java.util.ArrayList;

import mc.red.Gui.ClickGui.ClickGui;

public class CategoryManager {
	
	public static int currentPage = 0;
	
	public static void thisPage(int number) {
		currentPage=number;
		ArrayList<ClickGuiCategoryButton> category = ClickGui.getClickGuiCategoryButton();
		
		for(int i = 0; i< category.size();i++) {
			if(i != currentPage) {
				category.get(i).setIsOnThisPage(false);
			}
		}
	}


	
	
	
}
