SELECT s.scan_id, s.room_id, t.tag, t.rssi
FROM scans s
INNER JOIN scanned_tags t ON (s.scan_id = t.scan_id)