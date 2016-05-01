/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leeon.mobile.BBSBrowser;

import java.io.File;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A minimal "Hello, World!" application.
 */
public class ImageDetailActivity extends Activity {
	
	private File file;
	private String mime;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_image);
		
		file = (File)getIntent().getSerializableExtra("file");
		mime = getIntent().getStringExtra("mime");
		ImageDetailView v = (ImageDetailView)this.findViewById(R.id.viewImg);
		v.setImage(BitmapFactory.decodeFile(file.getAbsolutePath()));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "另存图片").setIcon(android.R.drawable.ic_menu_save);
        return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (file != null) UIUtil.chooseAndCopyFile(this, file, file.getName() + GIFOpenHelper.getImageFileType(mime));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
