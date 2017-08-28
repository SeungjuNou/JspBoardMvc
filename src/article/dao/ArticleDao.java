package article.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import article.model.Article;
import article.model.Writer;
import jdbc.JdbcUtil;

public class ArticleDao {

	public Article insert(Connection conn, Article article) throws SQLException {
		//article을 데이터베이스에 입력시켜주는 메서드 
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = conn.prepareStatement("insert into article (writer_id, writer_name, title, regdate, moddate, read_cnt) values (?,?,?,?,?,0)");
			pstmt.setString(1, article.getWriter().getId());
			pstmt.setString(2, article.getWriter().getName());
			pstmt.setString(3, article.getTitle());
			pstmt.setTimestamp(4, toTimestamp(article.getRegDate()));
			pstmt.setTimestamp(5, toTimestamp(article.getModifiedDate()));
			
			int insertedCount = pstmt.executeUpdate();

			if (insertedCount > 0) { //insert 삽입에 성공했을 경우 진입.
				
				stmt = conn.createStatement();
				rs = stmt.executeQuery("select last_insert_id() from article"); 
				//article 테이블에서 last_insert_id(마지막으로 insert된 primary key값)을 찾아온다.

				if (rs.next()) {
					
					Integer newNo = rs.getInt(1); //pk값을 nuwNo에 초기화한다.

					return new Article( 
					//Article메서드에 글번호,작성자,제목,작성일,수정일 등을 입력해서 반환해준다.
								newNo,
								article.getWriter(),
								article.getTitle(),
								article.getRegDate(),
								article.getModifiedDate(),
								0
								);
				}
			}
			return null;
		
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(stmt);
			JdbcUtil.close(pstmt);
		}
	} //insert 메서드 종료.


	private Timestamp toTimestamp(Date date) {
		return new Timestamp(date.getTime()); 
		//Date객체를 받아 Timestamp객체로 반환.
	}

	private Date toDate(Timestamp timestamp) {
		return new Date(timestamp.getTime());
		//Timestamp객체를 받아 Date객체로 반환.
	}


	public int selectCount(Connection conn) throws SQLException {
		//게시글의 수를 세어 반환하는 메서드.
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(*) from article");
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(stmt);
		}
	}//selectCount 메서드 종료.


	public List<Article> select(Connection conn, int startRow, int size) throws SQLException {
		//시작줄과 출력할 개수를 전달받아 Article 리스트를 생성해서 반환한다.
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = conn.prepareStatement("select * from article order by article_no desc limit ?, ?");
			pstmt.setInt(1, startRow);
			pstmt.setInt(2, size);
			rs = pstmt.executeQuery();
			List<Article> result = new ArrayList<>();
		
			while (rs.next()) {
				result.add(convertArticle(rs));
			}
		
			return result;
		
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
	}//select 메서드 종료.


	private Article convertArticle(ResultSet rs) throws SQLException {
		//받은 결과set인 rs를 전달받아 Article 객체에 저장해 반환.
		return new Article(rs.getInt("article_no"), 
			new Writer(
					rs.getString("writer_id"),
					rs.getString("writer_name")),
					rs.getString("title"),
					toDate(rs.getTimestamp("regdate")),
					toDate(rs.getTimestamp("moddate")),
					rs.getInt("read_cnt"));
	} //convertArticle 메서드 종료.
	

	public Article selectById(Connection conn, int no) throws SQLException {
		//전달받은 게시글 no에 해당하는 게시글을 가져와 반환해주는 메서드.
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = conn.prepareStatement("select * from article where article_no = ?");
			pstmt.setInt(1, no);
			rs = pstmt.executeQuery();
			Article article = null;
			
			if (rs.next()) {
				article = convertArticle(rs);
			}
			
			return article;
		
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
	} //selectById 메서드 종료.
	
	public void increaseReadCount(Connection conn, int no) throws SQLException {
		// 전달받은 게시글 no에 해당하는 게시글의 조회수를 증가시키는 메서드.
		try (PreparedStatement pstmt = conn.prepareStatement("update article set read_cnt = read_cnt + 1 where article_no = ?")) {
			pstmt.setInt(1, no);
			pstmt.executeUpdate();
		}
	} //increaseReadCount 메서드 종료.
	
	public int update(Connection conn, int no, String title) throws SQLException {
		// 전달받은 게시글 no에 해당하는 게시글의 title을 변경해주고, 현재시간을 수정된시간으로 저장하는 메서드.
		try (PreparedStatement pstmt = conn.prepareStatement("update article set title = ?, moddate = now() where article_no = ?")) {
			pstmt.setString(1, title);
			pstmt.setInt(2, no);
			return pstmt.executeUpdate();
		}
	} //update 메서드 종료.
}