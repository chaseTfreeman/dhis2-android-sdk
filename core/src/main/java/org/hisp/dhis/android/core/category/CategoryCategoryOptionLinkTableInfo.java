/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.category;

import org.hisp.dhis.android.core.arch.db.TableInfo;
import org.hisp.dhis.android.core.arch.db.tableinfos.LinkTableChildProjection;
import org.hisp.dhis.android.core.common.BaseModel;
import org.hisp.dhis.android.core.utils.Utils;

public final class CategoryCategoryOptionLinkTableInfo {



    public static final TableInfo TABLE_INFO = new TableInfo() {

        @Override
        public String name() {
            return "CategoryCategoryOptionLink";
        }

        @Override
        public Columns columns() {
            return new Columns();
        }
    };

    static final LinkTableChildProjection CHILD_PROJECTION = new LinkTableChildProjection(
            CategoryOptionTableInfo.TABLE_INFO,
            Columns.CATEGORY,
            Columns.CATEGORY_OPTION);

    private CategoryCategoryOptionLinkTableInfo() {
    }

    static class Columns extends BaseModel.Columns {

        static final String CATEGORY = "category";
        private static final String CATEGORY_OPTION = "categoryOption";
        private static final String SORT_ORDER = "sortOrder";

        @Override
        public String[] all() {
            return Utils.appendInNewArray(super.all(),
                    CATEGORY, CATEGORY_OPTION, SORT_ORDER);
        }

        @Override
        public String[] whereUpdate() {
            return all();
        }
    }
}
