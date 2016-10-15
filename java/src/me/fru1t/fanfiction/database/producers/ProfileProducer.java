package me.fru1t.fanfiction.database.producers;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.concurrent.DatabaseProducer;

public class ProfileProducer extends DatabaseProducer<ProfileProducer.Profile, Integer> {
	public static class Profile extends DatabaseProducer.Row<Integer> {
		public int ff_id;
		public String name;

		@Override
		public String toString() {
			return "Profile [ff_id=" + ff_id + ", name=" + name + "]";
		}
	}
	
	private static final String ID_NAME = "`user`.`id`";
	private static final int BUFFER_SIZE = 10000;
	
	public ProfileProducer() throws InterruptedException {
		super(ID_NAME, Profile.class, Boot.getDatabaseConnectionPool(),
				BUFFER_SIZE, Boot.getLogger());
	}

	@Override
	protected String getUnboundedQuery() {
		return "SELECT"
			+ " `user`.`id` AS `id`,"
			+ " `user`.`ff_id` AS `ff_id`,"
			+ " `user`.`name` AS `name`"
			+ " FROM `user`";
	}

}