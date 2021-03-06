package tv.zerator.ffs.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.jolbox.bonecp.PreparedStatementHandle;

import alexmog.apilib.dao.DAO;
import alexmog.apilib.managers.DaoManager.Dao;
import lombok.Data;
import tv.zerator.ffs.api.dao.beans.AccountBean;
import tv.zerator.ffs.api.dao.beans.AccountBean.BroadcasterType;
import tv.zerator.ffs.api.dao.beans.EventBean;

@Dao(database = "general")
public class EventsDao extends DAO {

	public int insert(EventBean data) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("INSERT INTO events "
						+ "(name, description, status, reserved_to_affiliates, reserved_to_partners, minimum_views, minimum_followers, ranking_type) VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
			prep.setString(1, data.getName());
			prep.setString(2, data.getDescription());
			prep.setString(3, data.getStatus().name());
			prep.setBoolean(4, data.isReservedToAffiliates());
			prep.setBoolean(5, data.isReservedToPartners());
			prep.setInt(6, data.getMinimumViews());
			prep.setInt(7, data.getMinimumFollowers());
			prep.setString(8, data.getRankingType().name());
			prep.executeUpdate();
			try (ResultSet rs = prep.getGeneratedKeys()) {
				if (rs.next()) return rs.getInt(1);
				throw new SQLException("Cannot insert element.");
			}
		}
	}
	
	private EventBean constructEvent(ResultSet rs) throws SQLException {
		EventBean bean = new EventBean();
		bean.setCurrent(rs.getBoolean("is_current"));
		bean.setDescription(rs.getString("description"));
		bean.setId(rs.getInt("id"));
		bean.setName(rs.getString("name"));
		bean.setReservedToAffiliates(rs.getBoolean("reserved_to_affiliates"));
		bean.setReservedToPartners(rs.getBoolean("reserved_to_partners"));
		bean.setStatus(EventBean.Status.valueOf(rs.getString("status")));
		bean.setRankingType(EventBean.RankingType.valueOf(rs.getString("ranking_type")));
		bean.setMinimumViews(rs.getInt("minimum_views"));
		bean.setMinimumFollowers(rs.getInt("minimum_followers"));
		return bean;
	}
	
	public List<EventBean> getEvents(EventBean.Status status, int start, int end) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT * FROM events WHERE status = ? ORDER BY id DESC LIMIT ?, ?")) {
			prep.setString(1, status.name());
			prep.setInt(2, start);
			prep.setInt(3, end);
			try (ResultSet rs = prep.executeQuery()) {
				List<EventBean> ret = new ArrayList<>();
				while (rs.next()) ret.add(constructEvent(rs));
				return ret;
			}
		}
	}
	
	public List<EventBean> getEvents(int start, int end) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT * FROM events ORDER BY id DESC LIMIT ?, ?")) {
			prep.setInt(1, start);
			prep.setInt(2, end);
			try (ResultSet rs = prep.executeQuery()) {
				List<EventBean> ret = new ArrayList<>();
				while (rs.next()) ret.add(constructEvent(rs));
				return ret;
			}
		}
	}
	
	public EventBean getEvent(int id) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT * FROM events WHERE id = ?")) {
			prep.setInt(1, id);
			try (ResultSet rs = prep.executeQuery()) {
				if (!rs.next()) return null;
				return constructEvent(rs);
			}
		}
	}
	
	public EventBean getCurrent() throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT * FROM events WHERE is_current = 1")) {
			try (ResultSet rs = prep.executeQuery()) {
				if (!rs.next()) return null;
				return constructEvent(rs);
			}
		}
	}

	public EventBean update(EventBean data) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("UPDATE events SET "
						+ "name = ?, description = ?, status = ?, reserved_to_affiliates = ?, "
						+ "reserved_to_partners = ?, is_current = ?, minimum_views = ?, minimum_followers = ?, ranking_type = ? WHERE id = ?")) {
			prep.setString(1, data.getName());
			prep.setString(2, data.getDescription());
			prep.setString(3, data.getStatus().name());
			prep.setBoolean(4, data.isReservedToAffiliates());
			prep.setBoolean(5, data.isReservedToPartners());
			prep.setBoolean(6, data.isCurrent());
			prep.setInt(7, data.getMinimumViews());
			prep.setInt(8, data.getMinimumFollowers());
			prep.setString(9, data.getRankingType().name());
			prep.setInt(10, data.getId());
			if (data.isCurrent()) {
				try (PreparedStatementHandle prep2 = (PreparedStatementHandle) getConnection().prepareStatement("UPDATE events SET "
						+ "is_current = 0 WHERE is_current = 1")) {
					prep2.executeUpdate();
				}
			}
			prep.executeUpdate();
		}
		return data;
	}
	
	public List<Integer> getRounds(int eventId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT round_id FROM event_rounds WHERE event_id = ?")) {
			prep.setInt(1, eventId);
			try (ResultSet rs = prep.executeQuery()) {
				List<Integer> ret = new ArrayList<>();
				while (rs.next()) ret.add(rs.getInt("round_id"));
				return ret;
			}
		}
	}
	
	public boolean roundExists(int eventId, int roundId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT round_id FROM event_rounds WHERE event_id = ? AND round_id = ?")) {
			prep.setInt(1, eventId);
			prep.setInt(2, roundId);
			try (ResultSet rs = prep.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	public int addRound(int eventId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("INSERT INTO event_rounds "
						+ "(event_id) VALUES "
						+ "(?)", Statement.RETURN_GENERATED_KEYS)) {
			prep.setInt(1, eventId);
			prep.executeUpdate();
			try (ResultSet rs = prep.getGeneratedKeys()) {
				if (rs.next()) return rs.getInt(1);
				throw new SQLException("Cannot insert element.");
			}
		}
	}
	
	public void deleteRound(int eventId, int roundId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("DELETE FROM event_rounds "
						+ "WHERE event_id = ? AND round_id = ?")) {
			prep.setInt(1, eventId);
			prep.setInt(2, roundId);
			prep.executeUpdate();
		}
	}
	
	public void addScore(int roundId, int accountId, double score) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("INSERT INTO round_scores "
						+ "(round_id, account_id, score) VALUES "
						+ "(?, ?, ?)")) {
			prep.setInt(1, roundId);
			prep.setInt(2, accountId);
			prep.setDouble(3, score);
			prep.executeUpdate();
		}
	}
	
	public void updateScore(int roundId, int accountId, double score) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("UPDATE round_scores SET "
						+ "score = ? WHERE round_id = ? AND account_id = ?")) {
			prep.setDouble(1, score);
			prep.setInt(2, roundId);
			prep.setInt(3, accountId);
			prep.executeUpdate();
		}
	}
	
	public @Data static class RoundUserScoreBean {
		private final String username, url, logo;
		private final int id;
		private final double score;
		public final int round;
	}
	
	public List<RoundUserScoreBean> getScores(int roundId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT s.score, a.username, a.url, a.twitch_id, a.logo FROM accounts a LEFT JOIN round_scores s ON s.account_id = a.twitch_id WHERE s.round_id = ?")) {
			prep.setInt(1, roundId);
			try (ResultSet rs = prep.executeQuery()) {
				List<RoundUserScoreBean> ret = new ArrayList<>();
				while (rs.next()) ret.add(new RoundUserScoreBean(rs.getString("a.username"), rs.getString("a.url"), rs.getString("a.logo"), rs.getInt("a.twitch_id"), rs.getDouble("s.score"), roundId));
				return ret;
			}
		}
	}
	
	public List<RoundUserScoreBean> getAllScores(int eventId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT s.score, s.round_id, a.username, a.url, a.twitch_id, a.logo FROM accounts a "
						+ "LEFT JOIN round_scores s ON s.account_id = a.twitch_id "
						+ "INNER JOIN event_rounds e ON s.round_id = e.round_id "
						+ "WHERE e.event_id = ?")) {
			prep.setInt(1, eventId);
			try (ResultSet rs = prep.executeQuery()) {
				List<RoundUserScoreBean> ret = new ArrayList<>();
				while (rs.next()) ret.add(new RoundUserScoreBean(rs.getString("a.username"), rs.getString("a.url"), rs.getString("a.logo"), rs.getInt("a.twitch_id"), rs.getDouble("s.score"), rs.getInt("s.round_id")));
				return ret;
			}
		}
	}
	
	public Double getScore(int roundId, int accountId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT score FROM round_scores WHERE round_id = ? AND account_id = ?")) {
			prep.setInt(1, roundId);
			prep.setInt(2, accountId);
			try (ResultSet rs = prep.executeQuery()) {
				if (rs.next()) return rs.getDouble("score");
				return null;
			}
		}
	}
	
	public List<AccountStatusBean> getUsers(int eventId, UserStatus status) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT a.twitch_id, a.username, a.email, a.views, a.followers, a.broadcaster_type, a.url, a.grade, a.logo, s.status, s.rank "
						+ " FROM accounts a LEFT JOIN account_event_status s ON s.account_id = a.twitch_id WHERE s.event_id = ? AND s.status = ?")) {
			prep.setInt(1, eventId);
			prep.setString(2, status.name());
			try (ResultSet rs = prep.executeQuery()) {
				List<AccountStatusBean> ret = new ArrayList<>();
				while (rs.next()) {
					AccountStatusBean bean = new AccountStatusBean();
					bean.setTwitchId(rs.getInt("a.twitch_id"));
					bean.setUsername(rs.getString("a.username"));
					bean.setEmail(rs.getString("a.email"));
					bean.setViews(rs.getInt("a.views"));
					bean.setFollowers(rs.getInt("a.followers"));
					bean.setBroadcasterType(BroadcasterType.valueOf(rs.getString("a.broadcaster_type")));
					bean.setUrl(rs.getString("a.url"));
					bean.setGrade(rs.getInt("a.grade"));
					bean.setLogo(rs.getString("a.logo"));
					bean.setStatus(UserStatus.valueOf(rs.getString("s.status")));
					bean.setRank(rs.getInt("s.rank"));
					bean.setSubscribeToWinner(rs.getBoolean("s.subscribe_to_winner"));
					ret.add(bean);
				}
				return ret;
			}
		}
	}
	
	public List<AccountStatusBean> getUsers(int eventId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT a.twitch_id, a.username, a.email, a.views, a.followers, a.broadcaster_type, a.url, a.grade, a.logo, s.status, s.rank, s.subscribe_to_winner "
						+ " FROM accounts a LEFT JOIN account_event_status s ON s.account_id = a.twitch_id WHERE s.event_id = ?")) {
			prep.setInt(1, eventId);
			try (ResultSet rs = prep.executeQuery()) {
				List<AccountStatusBean> ret = new ArrayList<>();
				while (rs.next()) {
					AccountStatusBean bean = new AccountStatusBean();
					bean.setTwitchId(rs.getInt("a.twitch_id"));
					bean.setUsername(rs.getString("a.username"));
					bean.setEmail(rs.getString("a.email"));
					bean.setViews(rs.getInt("a.views"));
					bean.setFollowers(rs.getInt("a.followers"));
					bean.setBroadcasterType(BroadcasterType.valueOf(rs.getString("a.broadcaster_type")));
					bean.setUrl(rs.getString("a.url"));
					bean.setGrade(rs.getInt("a.grade"));
					bean.setLogo(rs.getString("a.logo"));
					bean.setStatus(UserStatus.valueOf(rs.getString("s.status")));
					bean.setRank(rs.getInt("s.rank"));
					bean.setSubscribeToWinner(rs.getBoolean("s.subscribe_to_winner"));
					ret.add(bean);
				}
				return ret;
			}
		}
	}

	public void delete(int eventId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("DELETE FROM events WHERE id = ?")) {
			prep.setInt(1, eventId);
			prep.executeUpdate();
		}
	}
	
	public enum UserStatus {
		VALIDATED,
		AWAITING_FOR_EMAIL_VALIDATION,
		AWAITING_FOR_ADMIN_VALIDATION,
		REFUSED
	}

	public void registerUser(int eventId, int accountId, UserStatus status, String emailActivationKey) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("INSERT INTO account_event_status "
						+ "(account_id, event_id, status, email_activation_key) VALUES (?, ?, ?, ?)")) {
			prep.setInt(1, accountId);
			prep.setInt(2, eventId);
			prep.setString(3, status.name());
			prep.setString(4, emailActivationKey);
			prep.executeUpdate();
		}
	}
	
	public static @Data class UserStatusBean {
		private AccountBean account;
		private int eventId;
		private UserStatus status;
		private String emailActivationKey;
	}
	
	public UserStatusBean getUser(int eventId, int accountId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT s.event_id, s.status, s.email_activation_key, a.twitch_id, a.username, a.email, a.views, a.followers, a.broadcaster_type, a.url, a.grade, a.logo "
						+ " FROM accounts a LEFT JOIN account_event_status s ON s.account_id = a.twitch_id "
						+ "WHERE s.account_id = ? AND s.event_id = ?")) {
			prep.setInt(1, accountId);
			prep.setInt(2, eventId);
			try (ResultSet rs = prep.executeQuery()) {
				if (!rs.next()) return null;
				UserStatusBean bean = new UserStatusBean();
				AccountBean account = new AccountBean();
				account.setTwitchId(rs.getInt("a.twitch_id"));
				account.setUsername(rs.getString("a.username"));
				account.setEmail(rs.getString("a.email"));
				account.setViews(rs.getInt("a.views"));
				account.setFollowers(rs.getInt("a.followers"));
				account.setBroadcasterType(BroadcasterType.valueOf(rs.getString("a.broadcaster_type")));
				account.setUrl(rs.getString("a.url"));
				account.setGrade(rs.getInt("a.grade"));
				account.setLogo(rs.getString("a.logo"));
				bean.setAccount(account);
				bean.setEventId(rs.getInt("s.event_id"));
				bean.setStatus(UserStatus.valueOf(rs.getString("s.status")));
				bean.setEmailActivationKey(rs.getString("s.email_activation_key"));
				return bean;
			}
		}
	}
	
	public void removeUser(int eventId, int accountId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("DELETE FROM account_event_status WHERE account_id = ? AND event_id = ?")) {
			prep.setInt(1, accountId);
			prep.setInt(2, eventId);
			prep.executeUpdate();
		}
	}
	
	public void updateUser(int eventId, int accountId, UserStatus status) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("UPDATE account_event_status SET status = ? WHERE account_id = ? AND event_id = ?")) {
			prep.setString(1, status.name());
			prep.setInt(2, accountId);
			prep.setInt(3, eventId);
			prep.executeUpdate();
		}
	}

	public void updateUserRank(int eventId, int accountId, int rank) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("UPDATE account_event_status SET rank = ? WHERE account_id = ? AND event_id = ?")) {
			prep.setInt(1, rank);
			prep.setInt(2, accountId);
			prep.setInt(3, eventId);
			prep.executeUpdate();
		}
	}
	public void updateUserSubscriptionToWinner(int eventId, int accountId, boolean hasSubscribe) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("UPDATE account_event_status SET subscribe_to_winner = ? WHERE account_id = ? AND event_id = ?")) {
			prep.setBoolean(1, hasSubscribe);
			prep.setInt(2, accountId);
			prep.setInt(3, eventId);
			prep.executeUpdate();
		}
	}
	
	public @Data class AccountStatusBean extends AccountBean {
		public UserStatus status;
		public Integer rank;
		public boolean subscribeToWinner;
	}
	
	public AccountStatusBean getRegistered(int eventId, int accountId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT a.twitch_id, a.username, a.email, a.views, a.followers, a.broadcaster_type, a.url, a.grade, s.status, a.logo, s.rank, s.subscribe_to_winner "
						+ " FROM accounts a LEFT JOIN account_event_status s ON s.account_id = a.twitch_id WHERE s.event_id = ? AND a.twitch_id = ?")) {
			prep.setInt(1, eventId);
			prep.setInt(2, accountId);
			try (ResultSet rs = prep.executeQuery()) {
				if (rs.next()) {
					AccountStatusBean bean = new AccountStatusBean();
					bean.setTwitchId(rs.getInt("a.twitch_id"));
					bean.setUsername(rs.getString("a.username"));
					bean.setEmail(rs.getString("a.email"));
					bean.setViews(rs.getInt("a.views"));
					bean.setFollowers(rs.getInt("a.followers"));
					bean.setBroadcasterType(BroadcasterType.valueOf(rs.getString("a.broadcaster_type")));
					bean.setUrl(rs.getString("a.url"));
					bean.setGrade(rs.getInt("a.grade"));
					bean.setLogo(rs.getString("a.logo"));
					bean.status = UserStatus.valueOf(rs.getString("s.status"));
					bean.rank = rs.getInt("s.rank");
					bean.subscribeToWinner = rs.getBoolean("s.subscribe_to_winner");
					return bean;
				}
				return null;
			}
		}
	}
	
	public UserStatus getUserStatusFromEmailKey(int eventId, int accountId, String emailKey) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT status FROM account_event_status "
						+ "WHERE event_id = ? AND account_id = ? AND email_activation_key = ?")) {
			prep.setInt(1, eventId);
			prep.setInt(2, accountId);
			prep.setString(3, emailKey);
			try (ResultSet rs = prep.executeQuery()) {
				if (rs.next()) return UserStatus.valueOf(rs.getString("status"));
				return null;
			}
		}
	}

    public AccountStatusBean getUserFromRank(int eventId, int rank) throws SQLException {
        try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT a.twitch_id, a.username, a.email, a.views, a.followers, a.broadcaster_type, a.url, a.grade, s.status, a.logo, s.rank, s.subscribe_to_winner "
                + " FROM accounts a LEFT JOIN account_event_status s ON s.account_id = a.twitch_id WHERE s.event_id = ? AND s.rank = ?")) {
            prep.setInt(1, eventId);
            prep.setInt(2, rank);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    AccountStatusBean bean = new AccountStatusBean();
                    bean.setTwitchId(rs.getInt("a.twitch_id"));
                    bean.setUsername(rs.getString("a.username"));
                    bean.setEmail(rs.getString("a.email"));
                    bean.setViews(rs.getInt("a.views"));
                    bean.setFollowers(rs.getInt("a.followers"));
                    bean.setBroadcasterType(BroadcasterType.valueOf(rs.getString("a.broadcaster_type")));
                    bean.setUrl(rs.getString("a.url"));
                    bean.setGrade(rs.getInt("a.grade"));
                    bean.setLogo(rs.getString("a.logo"));
                    bean.status = UserStatus.valueOf(rs.getString("s.status"));
                    bean.rank = rs.getInt("s.rank");
                    bean.subscribeToWinner = rs.getBoolean("s.subscribe_to_winner");
                    return bean;
                }
                return null;
            }
        }
    }
	
	public static @Data class AccountEventBean {
		public EventBean event;
		public UserStatus status;
	}
	
	public List<AccountEventBean> getEventsForAccount(int accountId) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT s.status, e.id, e.name, e.description, e.status, e.reserved_to_affiliates, e.reserved_to_partners, e.is_current, e.ranking_type "
						+ "FROM events e LEFT JOIN account_event_status s ON s.event_id = e.id WHERE s.account_id = ?")) {
			prep.setInt(1, accountId);
			try (ResultSet rs = prep.executeQuery()) {
				List<AccountEventBean> ret = new ArrayList<>();
				while (rs.next()) {
				AccountEventBean bean = new AccountEventBean();
				EventBean event = new EventBean();
					event.setCurrent(rs.getBoolean("e.is_current"));
					event.setDescription(rs.getString("e.description"));
					event.setId(rs.getInt("e.id"));
					event.setName(rs.getString("e.name"));
					event.setReservedToAffiliates(rs.getBoolean("e.reserved_to_affiliates"));
					event.setReservedToPartners(rs.getBoolean("e.reserved_to_partners"));
					event.setStatus(EventBean.Status.valueOf(rs.getString("e.status")));
					event.setRankingType(EventBean.RankingType.valueOf(rs.getString("e.ranking_type")));
					bean.setEvent(event);
					bean.setStatus(UserStatus.valueOf(rs.getString("s.status")));
					ret.add(bean);
				}
				return ret;
			}
		}
	}
	
	public List<AccountEventBean> getEventsForAccountAndStatus(int accountId, UserStatus status) throws SQLException {
		try (PreparedStatementHandle prep = (PreparedStatementHandle) getConnection().prepareStatement("SELECT s.status, e.name, e.description, e.status, e.reserved_to_affiliates, e.reserved_to_partners, e.is_current, e.minimum_views, e.minimum_followers, e.ranking_type "
						+ "FROM events e LEFT JOIN account_event_status s ON s.event_id = e.id WHERE s.account_id = ? AND s.status = ?")) {
			prep.setInt(1, accountId);
			prep.setString(2, status.name());
			try (ResultSet rs = prep.executeQuery()) {
				List<AccountEventBean> ret = new ArrayList<>();
				while (rs.next()) {
				AccountEventBean bean = new AccountEventBean();
				EventBean event = new EventBean();
					event.setCurrent(rs.getBoolean("e.is_current"));
					event.setDescription(rs.getString("e.description"));
					event.setId(rs.getInt("e.id"));
					event.setName(rs.getString("e.name"));
					event.setReservedToAffiliates(rs.getBoolean("e.reserved_to_affiliates"));
					event.setReservedToPartners(rs.getBoolean("e.reserved_to_partners"));
					event.setStatus(EventBean.Status.valueOf(rs.getString("e.status")));
					event.setRankingType(EventBean.RankingType.valueOf(rs.getString("e.ranking_type")));
					event.setMinimumViews(rs.getInt("e.minimum_views"));
					event.setMinimumFollowers(rs.getInt("e.minimum_followers"));
					bean.setEvent(event);
					bean.setStatus(UserStatus.valueOf(rs.getString("s.status")));
					ret.add(bean);
				}
				return ret;
			}
		}
	}
}
