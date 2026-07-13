INSERT INTO data (id, filename, email, created_at)
VALUES ('a288509d-b2bc-4f1b-9e42-cccdc9083c8e', 'vacation_photo.jpg',        'alice@example.com',    '2026-06-01 10:15:00'),
       ('63c82f2f-6579-44dc-9ea9-f3b458377e78', 'contract_scan.png',         'bob@example.com',      '2026-06-02 14:30:00'),
       ('e43e432f-bf2c-4e01-a247-de255c12941d', 'bug_screenshot.png',        'charlie@example.com',  '2026-06-03 09:00:00'),
       ('13df3539-e90a-4167-b18c-245fed17f12c', 'profile_picture.jpg',       'alice@example.com',    '2026-06-04 11:45:00'),
       ('8b38a4a8-7c82-418e-a302-14e999fa460f', 'id_scan.png',               'david@example.com',    '2026-06-05 16:20:00'),
       ('1d89b634-8f4d-47b3-8ca9-83c1b4f670a1', 'dashboard_screenshot.png',  'bob@example.com',      '2026-06-06 08:10:00'),
       ('b1ccfbf3-d81c-4c8d-9832-7c7c7b870441', 'landscape_photo.jpg',      'elise@example.com',    '2026-06-07 13:00:00'),
       ('11754193-3aa5-4691-9c5b-6aeb9006ea4d', 'invoice_scan.jpg',          'charlie@example.com',  '2026-06-08 15:45:00'),
       ('116df5aa-d6ec-47a2-a8b1-16c27d652eef', 'birthday_photo.jpg',        'alice@example.com',    '2026-06-09 10:30:00'),
       ('ea18479a-489d-4708-b0a4-7bd8d7b73987', 'error_screenshot.png',      'david@example.com',    '2026-06-10 09:15:00'),
       ('920523c2-e7c8-4fd6-9dd6-01264751764e', 'sunset_photo.jpg',          'elise@example.com',    '2026-06-11 18:00:00'),
       ('cfde985e-b7b2-4af4-8558-3a8920de0a7f', 'diploma_scan.png',          'frank@example.com',    '2026-06-12 11:00:00'),
       ('c75753a7-5692-4efa-859b-56331e2d5d82', 'dog_photo.jpg',             'bob@example.com',      '2026-06-13 07:30:00'),
       ('1eacbca2-f8ba-466c-9c43-90a052c780e1', 'api_screenshot.png',        'alice@example.com',    '2026-06-14 14:20:00'),
       ('f3faef01-2354-4008-8891-03b5c8a30c9a', 'mountain_photo.jpg',        'charlie@example.com',  '2026-06-15 08:00:00'),
       ('84a5e811-db43-49fd-9fdb-a2cc7c16fd7d', 'receipt_scan.png',          'frank@example.com',    '2026-06-16 12:00:00')
ON CONFLICT (id) DO NOTHING;
