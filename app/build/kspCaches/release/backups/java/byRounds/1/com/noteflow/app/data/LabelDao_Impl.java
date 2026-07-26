package com.noteflow.app.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LabelDao_Impl implements LabelDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Label> __insertionAdapterOfLabel;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLabel;

  private final SharedSQLiteStatement __preparedStmtOfRenameLabelRefs;

  public LabelDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLabel = new EntityInsertionAdapter<Label>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `labels` (`name`) VALUES (?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Label entity) {
        statement.bindString(1, entity.getName());
      }
    };
    this.__preparedStmtOfDeleteLabel = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM labels WHERE name = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRenameLabelRefs = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE note_label_cross_ref SET labelName = ? WHERE labelName = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertLabel(final Label label, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLabel.insert(label);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLabel(final String name, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLabel.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteLabel.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object renameLabelRefs(final String oldName, final String newName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRenameLabelRefs.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, newName);
        _argIndex = 2;
        _stmt.bindString(_argIndex, oldName);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRenameLabelRefs.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Label>> observeLabels() {
    final String _sql = "SELECT * FROM labels ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"labels"}, new Callable<List<Label>>() {
      @Override
      @NonNull
      public List<Label> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final List<Label> _result = new ArrayList<Label>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Label _item;
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            _item = new Label(_tmpName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
