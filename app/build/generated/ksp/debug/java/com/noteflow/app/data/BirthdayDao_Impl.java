package com.noteflow.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
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
public final class BirthdayDao_Impl implements BirthdayDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Birthday> __insertionAdapterOfBirthday;

  private final EntityDeletionOrUpdateAdapter<Birthday> __deletionAdapterOfBirthday;

  private final EntityDeletionOrUpdateAdapter<Birthday> __updateAdapterOfBirthday;

  public BirthdayDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBirthday = new EntityInsertionAdapter<Birthday>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `birthdays` (`id`,`contactId`,`name`,`photoUri`,`month`,`day`,`year`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Birthday entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getContactId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getContactId());
        }
        statement.bindString(3, entity.getName());
        if (entity.getPhotoUri() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPhotoUri());
        }
        statement.bindLong(5, entity.getMonth());
        statement.bindLong(6, entity.getDay());
        if (entity.getYear() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getYear());
        }
      }
    };
    this.__deletionAdapterOfBirthday = new EntityDeletionOrUpdateAdapter<Birthday>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `birthdays` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Birthday entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBirthday = new EntityDeletionOrUpdateAdapter<Birthday>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `birthdays` SET `id` = ?,`contactId` = ?,`name` = ?,`photoUri` = ?,`month` = ?,`day` = ?,`year` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Birthday entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getContactId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getContactId());
        }
        statement.bindString(3, entity.getName());
        if (entity.getPhotoUri() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPhotoUri());
        }
        statement.bindLong(5, entity.getMonth());
        statement.bindLong(6, entity.getDay());
        if (entity.getYear() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getYear());
        }
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Birthday birthday, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBirthday.insertAndReturnId(birthday);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Birthday birthday, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBirthday.handle(birthday);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Birthday birthday, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBirthday.handle(birthday);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Birthday>> observeAll() {
    final String _sql = "SELECT * FROM birthdays ORDER BY month, day";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"birthdays"}, new Callable<List<Birthday>>() {
      @Override
      @NonNull
      public List<Birthday> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContactId = CursorUtil.getColumnIndexOrThrow(_cursor, "contactId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhotoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUri");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final List<Birthday> _result = new ArrayList<Birthday>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Birthday _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContactId;
            if (_cursor.isNull(_cursorIndexOfContactId)) {
              _tmpContactId = null;
            } else {
              _tmpContactId = _cursor.getString(_cursorIndexOfContactId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhotoUri;
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null;
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri);
            }
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpDay;
            _tmpDay = _cursor.getInt(_cursorIndexOfDay);
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            _item = new Birthday(_tmpId,_tmpContactId,_tmpName,_tmpPhotoUri,_tmpMonth,_tmpDay,_tmpYear);
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

  @Override
  public Object getAllOnce(final Continuation<? super List<Birthday>> $completion) {
    final String _sql = "SELECT * FROM birthdays";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Birthday>>() {
      @Override
      @NonNull
      public List<Birthday> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContactId = CursorUtil.getColumnIndexOrThrow(_cursor, "contactId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhotoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUri");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final List<Birthday> _result = new ArrayList<Birthday>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Birthday _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContactId;
            if (_cursor.isNull(_cursorIndexOfContactId)) {
              _tmpContactId = null;
            } else {
              _tmpContactId = _cursor.getString(_cursorIndexOfContactId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhotoUri;
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null;
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri);
            }
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpDay;
            _tmpDay = _cursor.getInt(_cursorIndexOfDay);
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            _item = new Birthday(_tmpId,_tmpContactId,_tmpName,_tmpPhotoUri,_tmpMonth,_tmpDay,_tmpYear);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long id, final Continuation<? super Birthday> $completion) {
    final String _sql = "SELECT * FROM birthdays WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Birthday>() {
      @Override
      @Nullable
      public Birthday call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContactId = CursorUtil.getColumnIndexOrThrow(_cursor, "contactId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhotoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUri");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final Birthday _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContactId;
            if (_cursor.isNull(_cursorIndexOfContactId)) {
              _tmpContactId = null;
            } else {
              _tmpContactId = _cursor.getString(_cursorIndexOfContactId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhotoUri;
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null;
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri);
            }
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpDay;
            _tmpDay = _cursor.getInt(_cursorIndexOfDay);
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            _result = new Birthday(_tmpId,_tmpContactId,_tmpName,_tmpPhotoUri,_tmpMonth,_tmpDay,_tmpYear);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
