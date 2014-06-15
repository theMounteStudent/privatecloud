package ch.ffhs.privatecloudffhs.adapter;

import java.util.List;

import ch.ffhs.privatecloudffhs.R;
import ch.ffhs.privatecloudffhs.database.Folder;
import ch.ffhs.privatecloudffhs.database.PrivateCloudDatabase;
import ch.ffhs.privatecloudffhs.database.SyncFile;
import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.MediaStore.Files;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressLint("NewApi")
public class ConflictListAdapter extends ArrayAdapter<SyncFile>{	
	Context context;
	LayoutInflater inflater;
	List<SyncFile> list;
	SparseBooleanArray mSelectedItemsIds;
	PrivateCloudDatabase db;

	public ConflictListAdapter(Context context) {
		super(context, 0);
		
		mSelectedItemsIds = new SparseBooleanArray();
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_conflict, null);
			
			holder.path = (TextView) convertView.findViewById(R.id.Conflict_List_Path);
			holder.server = (TextView) convertView.findViewById(R.id.Conflict_List_Server);
			
			convertView.setTag(holder);
		} 
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.path.setText(list.get(position).getPath());		
		Folder folder = db.getFolder(list.get(position).getFolderId());
		holder.server.setText(db.getServer(folder.getServerId()).getServername());
		return convertView;
	}
	
	/*public void toggleSelection(int position) {
		selectView(position, !mSelectedItemsIds.get(position));
	}

	public void selectView(int position, boolean value) {
		if (value)
			mSelectedItemsIds.put(position, value);
		else
			mSelectedItemsIds.delete(position);
		notifyDataSetChanged();
	}
	
	public void removeSelection() {
		mSelectedItemsIds = new SparseBooleanArray();
		notifyDataSetChanged();
	}
	
	public SparseBooleanArray getSelectedIds() {
		return mSelectedItemsIds;
	}
	*/
	
	public void refreshList(List<SyncFile> list) {
		this.list = list;

		clear();
		addAll(this.list);
		
		notifyDataSetChanged();
	}

	
	private class ViewHolder {
		TextView path;
		TextView server;
	}
}
