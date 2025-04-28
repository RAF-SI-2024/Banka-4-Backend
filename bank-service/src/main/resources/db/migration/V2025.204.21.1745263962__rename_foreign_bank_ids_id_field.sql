-- Rename ForeignBankId#{userId -> id}
alter table otc_requests rename column made_by_user_id to made_by_id;
alter table otc_requests rename column made_for_user_id to made_for_id;
alter table otc_requests rename column modified_by_user_id to modified_by_id;
