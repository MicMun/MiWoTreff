/**
 * BackupActionProvider.java
 *
 * Copyright 2012 by MicMun
 */
package de.micmun.android.miwotreff;

import android.content.Context;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;

/**
 * ActionProvider for the backup/restore item in actionbar.
 *
 * @author MicMun
 * @version 1.0, 28.12.2012
 */
public class BackupActionProvider
extends ActionProvider
implements OnMenuItemClickListener
{
   private MainActivity mCtx; // Context = MainActivity
   private String backup = ""; // Title Backup
   private String restore = ""; // Title Restore
   
   /**
    * Creates a new BackupActionProvider with the context.
    * 
    * @param  context
    *         Context of the application.
    */
   public BackupActionProvider(Context context) {
      super(context);
      if (context instanceof MainActivity)
         mCtx = (MainActivity)context;
      else
         mCtx = null;
   }
   
   /**
    * @see android.view.ActionProvider#onCreateActionView()
    */
   @Override
   public View onCreateActionView() {
      return null;
   }
   
   /**
    * @see android.view.ActionProvider#onPerformDefaultAction()
    */
   @Override
   public boolean onPerformDefaultAction() {
      return super.onPerformDefaultAction();      
   }
   
   /**
    * @see android.view.ActionProvider#hasSubMenu()
    */
   @Override
   public boolean hasSubMenu() {
      return true;
   }
   
   /**
    * @see android.view.ActionProvider#onPrepareSubMenu(android.view.SubMenu)
    */
   @Override
   public void onPrepareSubMenu(SubMenu subMenu) {
      subMenu.clear();
      MenuItem item1 = subMenu.add(R.string.label_backup);
      item1.setOnMenuItemClickListener(this);
      backup = item1.getTitle().toString();
      MenuItem item2 = subMenu.add(R.string.label_restore);
      item2.setOnMenuItemClickListener(this);
      restore = item2.getTitle().toString();
   }
   
   /**
    * @see android.view.MenuItem.OnMenuItemClickListener#onMenuItemClick(android.view.MenuItem)
    */
   @Override
   public boolean onMenuItemClick(MenuItem item) {
      if (mCtx == null) {
         return false;
      }
      
      if (item.getTitle().toString().equals(backup))
         mCtx.backupOrRestore(MainActivity.TYPE_BACKUP);
      else if (item.getTitle().toString().equals(restore))
         mCtx.backupOrRestore(MainActivity.TYPE_RESTORE);
      
      return true;
   }
}
