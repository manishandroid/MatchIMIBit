package com.matchimi.registration;

import java.io.File;

abstract class AlbumStorageDirFactory {
	protected abstract File getAlbumStorageDir(String albumName);
}
