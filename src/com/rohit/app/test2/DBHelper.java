package com.rohit.app.test2;

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class DBHelper {
	private static final String DATABASE_NAME = "RohitMyGalleryApp.db";
	private static final int DATABASE_VERSION = 1;
	private static final String IMAGES_TABLE_NAME = "Images";

	public static final String LABEL = "label";
	public static final String PIC = "pic";
	public static final String ID = "picid";

	private static final String CREATE_IMAGES_TABLE_QUERY = "create table "
			+ IMAGES_TABLE_NAME + "(" + ID + " integer primary key, " + LABEL
			+ " text not null, " + PIC + " blob not null" + ");";

	private class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_IMAGES_TABLE_QUERY);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			db.execSQL("DROP TABLE IF EXISTS " + IMAGES_TABLE_NAME);
			onCreate(db);
		}
	}

	public DBHelper(Context context) {
		databaseHelper = new DatabaseHelper(context);
	}

	private DatabaseHelper databaseHelper;

	public void addImage(String label, Bitmap bitmap) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(LABEL, label);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 10, stream);
		contentValues.put(PIC, stream.toByteArray());
		databaseHelper.getWritableDatabase().insert(IMAGES_TABLE_NAME, null,
				contentValues);
	}

	public int numberOfRows() {
		Cursor cursor = databaseHelper.getReadableDatabase().query(
				IMAGES_TABLE_NAME, new String[] { ID }, null, null, null, null,
				null, null);
		if (null != cursor) {
			return cursor.getCount();
		}
		return 0;
	}

	public IdAndLabelEncapsulated[] getIdAndLabels() {
		Cursor cursor = databaseHelper.getReadableDatabase().query(
				IMAGES_TABLE_NAME, new String[] { ID, LABEL }, null, null,
				null, null, null, null);
		if (null != cursor) {
			IdAndLabelEncapsulated[] array = new IdAndLabelEncapsulated[cursor
					.getCount()];
			int index = 0;
			while (cursor.moveToNext()) {
				if (cursor != null) {
					array[index] = new IdAndLabelEncapsulated();
					array[index].setId(cursor.getInt(cursor
							.getColumnIndex(DBHelper.ID)));
					array[index].setLabel(cursor.getString(cursor
							.getColumnIndex(DBHelper.LABEL)));
					index++;
				}
			}
			return array;
		}
		return null;
	}

	public Bitmap getImage(int rowIndex, int size) {
		Cursor cursor = databaseHelper.getReadableDatabase().query(
				IMAGES_TABLE_NAME, new String[] { PIC }, ID + " = ?",
				new String[] { String.valueOf(rowIndex) }, null, null, null,
				null);
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		if (null != cursor) {
			cursor.moveToNext();
			if (cursor != null) {
				byte[] array = cursor.getBlob(cursor
						.getColumnIndex(DBHelper.PIC));
				BitmapFactory.decodeByteArray(array, 0, array.length, options);
				int insamplesize = 1;
				int height = options.outHeight;
				int width = options.outWidth;
				if(height > size || width > size){
					int halfHeight = height / 2;
					int halfWidth = width / 2;
					while((halfHeight / insamplesize) > size && (halfWidth/insamplesize) > size){
						insamplesize *= 2;
					}
				}
				options.inSampleSize = insamplesize;
				options.inJustDecodeBounds = false;
				return BitmapFactory.decodeByteArray(array, 0, array.length, options);
			}
		}
		return null;
	}

}
