package com.noteflow.app.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile NoteDao _noteDao;

  private volatile LabelDao _labelDao;

  private volatile BirthdayDao _birthdayDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `color` TEXT NOT NULL, `pinned` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `inTrash` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `modifiedAt` INTEGER NOT NULL, `deletedAt` INTEGER, `reminderAt` INTEGER, `passwordHash` TEXT, `passwordSalt` TEXT, `calendarDate` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `checklist_items` (`noteId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `text` TEXT NOT NULL, `checked` INTEGER NOT NULL, PRIMARY KEY(`noteId`, `position`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `note_images` (`noteId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `uri` TEXT NOT NULL, PRIMARY KEY(`noteId`, `position`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `labels` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `note_label_cross_ref` (`noteId` INTEGER NOT NULL, `labelName` TEXT NOT NULL, PRIMARY KEY(`noteId`, `labelName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `birthdays` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contactId` TEXT, `name` TEXT NOT NULL, `photoUri` TEXT, `month` INTEGER NOT NULL, `day` INTEGER NOT NULL, `year` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd8f44844ea23818d769c787b5f0c087d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `notes`");
        db.execSQL("DROP TABLE IF EXISTS `checklist_items`");
        db.execSQL("DROP TABLE IF EXISTS `note_images`");
        db.execSQL("DROP TABLE IF EXISTS `labels`");
        db.execSQL("DROP TABLE IF EXISTS `note_label_cross_ref`");
        db.execSQL("DROP TABLE IF EXISTS `birthdays`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsNotes = new HashMap<String, TableInfo.Column>(15);
        _columnsNotes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("body", new TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("color", new TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("pinned", new TableInfo.Column("pinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("archived", new TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("inTrash", new TableInfo.Column("inTrash", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("modifiedAt", new TableInfo.Column("modifiedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("reminderAt", new TableInfo.Column("reminderAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("passwordHash", new TableInfo.Column("passwordHash", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("passwordSalt", new TableInfo.Column("passwordSalt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("calendarDate", new TableInfo.Column("calendarDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNotes = new TableInfo("notes", _columnsNotes, _foreignKeysNotes, _indicesNotes);
        final TableInfo _existingNotes = TableInfo.read(db, "notes");
        if (!_infoNotes.equals(_existingNotes)) {
          return new RoomOpenHelper.ValidationResult(false, "notes(com.noteflow.app.data.Note).\n"
                  + " Expected:\n" + _infoNotes + "\n"
                  + " Found:\n" + _existingNotes);
        }
        final HashMap<String, TableInfo.Column> _columnsChecklistItems = new HashMap<String, TableInfo.Column>(4);
        _columnsChecklistItems.put("noteId", new TableInfo.Column("noteId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChecklistItems.put("position", new TableInfo.Column("position", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChecklistItems.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChecklistItems.put("checked", new TableInfo.Column("checked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChecklistItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChecklistItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChecklistItems = new TableInfo("checklist_items", _columnsChecklistItems, _foreignKeysChecklistItems, _indicesChecklistItems);
        final TableInfo _existingChecklistItems = TableInfo.read(db, "checklist_items");
        if (!_infoChecklistItems.equals(_existingChecklistItems)) {
          return new RoomOpenHelper.ValidationResult(false, "checklist_items(com.noteflow.app.data.ChecklistItem).\n"
                  + " Expected:\n" + _infoChecklistItems + "\n"
                  + " Found:\n" + _existingChecklistItems);
        }
        final HashMap<String, TableInfo.Column> _columnsNoteImages = new HashMap<String, TableInfo.Column>(3);
        _columnsNoteImages.put("noteId", new TableInfo.Column("noteId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNoteImages.put("position", new TableInfo.Column("position", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNoteImages.put("uri", new TableInfo.Column("uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNoteImages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNoteImages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNoteImages = new TableInfo("note_images", _columnsNoteImages, _foreignKeysNoteImages, _indicesNoteImages);
        final TableInfo _existingNoteImages = TableInfo.read(db, "note_images");
        if (!_infoNoteImages.equals(_existingNoteImages)) {
          return new RoomOpenHelper.ValidationResult(false, "note_images(com.noteflow.app.data.NoteImage).\n"
                  + " Expected:\n" + _infoNoteImages + "\n"
                  + " Found:\n" + _existingNoteImages);
        }
        final HashMap<String, TableInfo.Column> _columnsLabels = new HashMap<String, TableInfo.Column>(1);
        _columnsLabels.put("name", new TableInfo.Column("name", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLabels = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLabels = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLabels = new TableInfo("labels", _columnsLabels, _foreignKeysLabels, _indicesLabels);
        final TableInfo _existingLabels = TableInfo.read(db, "labels");
        if (!_infoLabels.equals(_existingLabels)) {
          return new RoomOpenHelper.ValidationResult(false, "labels(com.noteflow.app.data.Label).\n"
                  + " Expected:\n" + _infoLabels + "\n"
                  + " Found:\n" + _existingLabels);
        }
        final HashMap<String, TableInfo.Column> _columnsNoteLabelCrossRef = new HashMap<String, TableInfo.Column>(2);
        _columnsNoteLabelCrossRef.put("noteId", new TableInfo.Column("noteId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNoteLabelCrossRef.put("labelName", new TableInfo.Column("labelName", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNoteLabelCrossRef = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNoteLabelCrossRef = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNoteLabelCrossRef = new TableInfo("note_label_cross_ref", _columnsNoteLabelCrossRef, _foreignKeysNoteLabelCrossRef, _indicesNoteLabelCrossRef);
        final TableInfo _existingNoteLabelCrossRef = TableInfo.read(db, "note_label_cross_ref");
        if (!_infoNoteLabelCrossRef.equals(_existingNoteLabelCrossRef)) {
          return new RoomOpenHelper.ValidationResult(false, "note_label_cross_ref(com.noteflow.app.data.NoteLabelCrossRef).\n"
                  + " Expected:\n" + _infoNoteLabelCrossRef + "\n"
                  + " Found:\n" + _existingNoteLabelCrossRef);
        }
        final HashMap<String, TableInfo.Column> _columnsBirthdays = new HashMap<String, TableInfo.Column>(7);
        _columnsBirthdays.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBirthdays.put("contactId", new TableInfo.Column("contactId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBirthdays.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBirthdays.put("photoUri", new TableInfo.Column("photoUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBirthdays.put("month", new TableInfo.Column("month", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBirthdays.put("day", new TableInfo.Column("day", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBirthdays.put("year", new TableInfo.Column("year", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBirthdays = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBirthdays = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBirthdays = new TableInfo("birthdays", _columnsBirthdays, _foreignKeysBirthdays, _indicesBirthdays);
        final TableInfo _existingBirthdays = TableInfo.read(db, "birthdays");
        if (!_infoBirthdays.equals(_existingBirthdays)) {
          return new RoomOpenHelper.ValidationResult(false, "birthdays(com.noteflow.app.data.Birthday).\n"
                  + " Expected:\n" + _infoBirthdays + "\n"
                  + " Found:\n" + _existingBirthdays);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d8f44844ea23818d769c787b5f0c087d", "6154bc838540ffeafc822c22dca55ccc");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "notes","checklist_items","note_images","labels","note_label_cross_ref","birthdays");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `notes`");
      _db.execSQL("DELETE FROM `checklist_items`");
      _db.execSQL("DELETE FROM `note_images`");
      _db.execSQL("DELETE FROM `labels`");
      _db.execSQL("DELETE FROM `note_label_cross_ref`");
      _db.execSQL("DELETE FROM `birthdays`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(NoteDao.class, NoteDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LabelDao.class, LabelDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BirthdayDao.class, BirthdayDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public NoteDao noteDao() {
    if (_noteDao != null) {
      return _noteDao;
    } else {
      synchronized(this) {
        if(_noteDao == null) {
          _noteDao = new NoteDao_Impl(this);
        }
        return _noteDao;
      }
    }
  }

  @Override
  public LabelDao labelDao() {
    if (_labelDao != null) {
      return _labelDao;
    } else {
      synchronized(this) {
        if(_labelDao == null) {
          _labelDao = new LabelDao_Impl(this);
        }
        return _labelDao;
      }
    }
  }

  @Override
  public BirthdayDao birthdayDao() {
    if (_birthdayDao != null) {
      return _birthdayDao;
    } else {
      synchronized(this) {
        if(_birthdayDao == null) {
          _birthdayDao = new BirthdayDao_Impl(this);
        }
        return _birthdayDao;
      }
    }
  }
}
