create type if not exists comment (
	author_name text,
	author_profile_url text,
	author_picture_url text,
	content text,
	comment_likes list<frozen<like>>,
	created_at timestamp
);

create type if not exists like (
	author_name text,
	author_profile_url text,
	author_picture_url text,
	created_at timestamp
);

create table if not exists chat
(
	id uuid primary key,
	name text,
	updated_at timestamp
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1;

create table if not exists chat_message
(
	chat_id uuid,
	from_user text,
	created_at timestamp,
	message text,
	primary key ((chat_id, from_user), created_at)
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1;

create table if not exists page
(
	id bigint primary key,
	admin_email text,
	last_active timestamp,
	name text,
	profile_path text,
	profile_photo_path text
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1
	and default_time_to_live = 1209600;

create table if not exists page_post
(
	page_id bigint,
	content_type text,
	created_at timestamp,
	comments list<frozen<comment>>,
	content text,
	likes list<frozen<like>>,
	page_name text,
	primary key ((page_id, content_type), created_at)
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1;

create table if not exists profile_link_dictionary
(
	path text primary key,
	page_id bigint,
	user_email text
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1
	and default_time_to_live = 1209600;

create table if not exists user
(
	email text primary key,
	created_at timestamp,
	last_active timestamp,
	name text,
	owned_pages list<int>,
	password_hash text,
	profile_path text,
	profile_photo_path text,
	status boolean,
	surname text
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1
	and default_time_to_live = 1209600;

create table if not exists user_chat
(
	chat_id uuid primary key,
	created_at timestamp,
	user_email text
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1;

create table if not exists user_follower
(
	user_email text,
	follower_email text,
	created_at timestamp,
	primary key (user_email, follower_email)
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1;

create table if not exists user_follows
(
	follows_email text,
	user_email text,
	created_at timestamp,
	primary key (follows_email, user_email)
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1;

create table if not exists user_post
(
	user_email text,
	content_type text,
	created_at timestamp,
	comments list<frozen<comment>>,
	content text,
	likes list<frozen<like>>,
	user_profile_path text,
	primary key ((user_email, content_type), created_at)
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
	and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
	and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
	and dclocal_read_repair_chance = 0.1;
