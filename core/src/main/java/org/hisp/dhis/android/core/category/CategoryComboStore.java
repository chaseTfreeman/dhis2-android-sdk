package org.hisp.dhis.android.core.category;


import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.arch.db.binders.IdentifiableStatementBinder;
import org.hisp.dhis.android.core.arch.db.binders.StatementBinder;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.common.StoreFactory;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

public final class CategoryComboStore {

    private CategoryComboStore() {}

    private static StatementBinder<CategoryCombo> BINDER = new IdentifiableStatementBinder<CategoryCombo>() {
        @Override
        public void bindToStatement(@NonNull CategoryCombo o, @NonNull SQLiteStatement sqLiteStatement) {
            super.bindToStatement(o, sqLiteStatement);
            sqLiteBind(sqLiteStatement, 7, o.isDefault());
        }
    };

    public static IdentifiableObjectStore<CategoryCombo> create(DatabaseAdapter databaseAdapter) {
        return StoreFactory.objectWithUidStore(databaseAdapter, CategoryComboTableInfo.TABLE_INFO, BINDER);
    }
}
