package org.twightlight.talents.menus.buttons.melee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.twightlight.talents.Talents;
import org.twightlight.talents.database.SQLite;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.ChatSessionService;
import org.twightlight.talents.menus.PlayerMenu;
import org.twightlight.talents.menus.TalentsMenu;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.Utility;

public class MAS {
   private int x = -1;
   private int y = 12;
   private TalentsCategory category;
   private String id;

   public MAS() {
      this.category = TalentsCategory.Melee;
      this.id = "MAS";
      Button button = new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         SQLite database = Talents.getInstance().getDatabase();
         int level = database.getTalentLevel(p, this.category, this.id);
         Talent<?> talent = (Talent)((HashMap)Talents.getInstance().getTalentsManagerService().Talents.get(this.category.getColumn())).get(this.id);
         List<Integer> costlist = talent.getCostList();
         if (database.getTalentLevel(p, TalentsCategory.Melee, "AP") < 10) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn cần nâng cấp các tài năng trước đó lên cấp cần thiết trước đã!"));
         } else if (level >= costlist.size()) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã max điểm tài năng này!"));
         } else {
            if (e.isLeftClick()) {
               int soulstones = database.getSoulStones(p);
               if (soulstones >= Utility.totalCost(costlist, level, level)) {
                  database.setSoulStones(p, soulstones - Utility.totalCost(costlist, level, level));
                  database.upgradeTalents(1, talent, this.id, p);
                  p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công điểm tài năng này"));
                  if (PlayerMenu.getInstance(p) != null) {
                     PlayerMenu menu = PlayerMenu.getInstance(p);
                     menu.getHolder().open();
                  }
               } else {
                  p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không đủ đá linh hồn!"));
               }
            } else if (e.isRightClick()) {
               p.closeInventory();
               PlayerMenu menux = PlayerMenu.getInstance(p);
               p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNhập một số nguyên tương ứng với số cấp độ bạn muốn nâng. Nhập 'cancel' để bỏ qua!"));
               ChatSessionService.createSession(p, (s) -> {
                  if (s.equals("cancel")) {
                     ChatSessionService.end(p);
                     p.openInventory(e.getClickedInventory());
                  } else {
                     if (!Utility.isInteger(s)) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn phải nhập một số nguyên!"));
                        p.openInventory(e.getClickedInventory());
                     } else {
                        int increment = Integer.parseInt(s);
                        if (level + increment > costlist.size()) {
                           p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn nhập số hơi lớn rồi đó! Tối đa là " + (costlist.size() - level)));
                           p.openInventory(e.getClickedInventory());
                        } else {
                           int totalCost = Utility.totalCost(costlist, level, level + increment - 1);
                           if (database.getSoulStones(p) < totalCost) {
                              p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không đủ đá linh hồn!"));
                              p.openInventory(e.getClickedInventory());
                           } else {
                              database.setSoulStones(p, database.getSoulStones(p) - totalCost);
                              database.upgradeTalents(increment, talent, this.id, p);
                              p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công điểm tài năng này"));
                              menux.getHolder().open();
                           }
                        }
                     }

                     ChatSessionService.end(p);
                  }
               });
            }

         }
      }, (player) -> {
         int level = Talents.getInstance().getDatabase().getTalentLevel(player, this.category, this.id);
         List<String> lore = new ArrayList();
         lore.add("&7Cận chiến có tỷ lệ " + Utility.toDecimal((double)(level * 4)) + "% được giảm 35%.");
         lore.add("&7thời gian hồi đòn đánh.");
         List<Integer> costlist = ((Talent)((HashMap)Talents.getInstance().getTalentsManagerService().Talents.get(this.category.getColumn())).get(this.id)).getCostList();
         boolean enchanted = false;
         boolean req = true;
         boolean first = true;
         if (Talents.getInstance().getDatabase().getTalentLevel(player, TalentsCategory.Melee, "AP") < 10) {
            if (first) {
               lore.add("");
               lore.add("&cYêu cầu:");
               first = false;
            }

            lore.add("&cPhá màng trinh X");
            req = false;
         }

         if (req) {
            if (level >= costlist.size()) {
               enchanted = true;
               lore.add("");
               lore.add("&aBạn đã nâng điểm tài năng này lên tối đa");
            } else {
               lore.add("");
               lore.add("&eBạn cần &d" + costlist.get(level) + " &eđá linh hồn để nâng cấp!");
               lore.add("&bChuột phải để nâng cấp nhanh!");
            }
         } else {
            lore.add("");
            lore.add("&cBạn chưa đủ điều kiện để nâng cấp tài năng này!");
         }

         String roman = Utility.toRoman(level);
         if (!roman.isEmpty()) {
            roman = " " + roman;
         }

         return ConversionUtil.createItem(Material.SHEARS, level, "", 0, "&eTấn công nhanh" + roman, lore, enchanted);
      });
      TalentsMenu.setItem(this.x, this.y, button);
   }
}
