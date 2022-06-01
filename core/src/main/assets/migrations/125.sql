# Add restriction (ANDROSDK-1502);

ALTER TABLE EventDataFilter RENAME TO EventDataFilter_Old;
CREATE TABLE ItemFilter (_id INTEGER PRIMARY KEY AUTOINCREMENT, eventFilter TEXT, dataItem TEXT, trackedEntityInstanceFilter TEXT, attribute TEXT, sw TEXT, ew TEXT, le TEXT, ge TEXT, gt TEXT, lt TEXT, eq TEXT, inProperty TEXT, like TEXT, dateFilter TEXT, FOREIGN KEY (eventFilter) REFERENCES EventFilter (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (trackedEntityInstanceFilter) REFERENCES TrackedEntityInstanceFilter (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED);
INSERT INTO ItemFilter (_id, eventFilter, dataItem, le, ge, gt, lt, eq, inProperty, like, dateFilter) SELECT _id, eventFilter, dataItem, le, ge, gt, lt, eq, inProperty, like, dateFilter FROM EventDataFilter_Old;
DROP TABLE IF EXISTS  EventDataFilter_Old;
