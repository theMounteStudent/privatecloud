package ch.ffhs.privatecloudffhs.sync;

import java.io.File;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp.LsEntry;

import ch.ffhs.privatecloudffhs.database.SyncFile;

public interface SyncConnection {
	public SyncFile uploadFile(File file, SyncFile syncFile);
	public SyncFile downloadFile(File file, SyncFile syncFile);
	public String getRemoteCheckSum(String filePath);
	public void mkDir(String folderName);
	public void initFolderSync(String directory);
	public Boolean isConnected();
	public Vector<LsEntry> listRemoteDir(String path);
}
