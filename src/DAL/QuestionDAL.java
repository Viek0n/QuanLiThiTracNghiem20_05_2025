package DAL;

import DTO.AnswerDTO;
import DTO.ChapterDTO;
import DTO.QuestionDTO;
import DTO.UserDTO;
import MICS.Connect;
import MICS.Enums;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionDAL {
    final private ChapterDAL chapterDAL;
    final private AnswerDAL answerDAL;
    final private SubjectDAL subjectDAL;

    public QuestionDAL() {
        chapterDAL = new ChapterDAL();
        answerDAL = new AnswerDAL();
        subjectDAL = new SubjectDAL();
    }

    public ArrayList<QuestionDTO> getAll() {
        ArrayList<QuestionDTO> array = new ArrayList<>();
        String sql = "SELECT * FROM cauhoi";
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                QuestionDTO ques = new QuestionDTO(rs.getString("MaCH"),
                        chapterDAL.get(rs.getString("MaChuong")),
                        rs.getString("MaND"),
                        Enums.DifficultValue.valueOf(rs.getString("DoKho")),
                        rs.getString("NoiDung"));
                ques.setAns(answerDAL.getAllByQId(ques.getID()));
                ques.setSubject(subjectDAL.getByChapID(ques.getChapter().getID()));
                array.add(ques);
            }
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại!");
            e.printStackTrace();
        }
        return array;
    }

    //Get
    public QuestionDTO getByID(String ID) {
        String sql = "SELECT * FROM cauhoi WHERE MaCH = ?";
        try (Connection conn = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                QuestionDTO newQues = new QuestionDTO(rs.getString("MaCH"),
                        chapterDAL.get(rs.getString("MaChuong")),
                        rs.getString("MaND"),
                        Enums.DifficultValue.valueOf(rs.getString("DoKho")),
                        rs.getString("NoiDung"));
                newQues.setAns(answerDAL.getAllByQId(newQues.getID()));
                newQues.setSubject(subjectDAL.getByChapID(newQues.getChapter().getID()));
                return newQues;
            }
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại!");
            e.printStackTrace();
        }
        return null;
    }

    public String getNextId() {
        String sql = "SELECT * FROM cauhoi ORDER BY CAST(SUBSTRING(MaCH, 3) AS UNSIGNED) DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString(1);
                int number = Integer.parseInt(lastId.substring(2));
                String nextId = "CH" + (number + 1);
                return nextId;
            }
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại! Không tìm được Id tiếp theo");
            e.printStackTrace();
        }
        return "";
    }

    public ArrayList<QuestionDTO> search(String keyword) {
        ArrayList<QuestionDTO> results = new ArrayList<>();
        String sql = "SELECT * FROM cauhoi WHERE MaCH LIKE ? OR NoiDung LIKE ?";
        try (Connection conn = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                QuestionDTO newQues = new QuestionDTO(rs.getString("MaCH"),
                        chapterDAL.get(rs.getString("MaChuong")),
                        rs.getString("MaND"),
                        Enums.DifficultValue.valueOf(rs.getString("DoKho")),
                        rs.getString("NoiDung"));
                        newQues.setAns(answerDAL.getAllByQId(newQues.getID()));
                        newQues.setSubject(subjectDAL.getByChapID(newQues.getChapter().getID()));
                results.add(newQues);
            }
        } catch (SQLException e) {
            System.out.println("Tìm kiếm câu hỏi thất bại!");
            e.printStackTrace();
        }
        return results;
    }

    //Update
    public Boolean update(QuestionDTO a) {
        String sql = "UPDATE cauhoi SET NoiDung = ?, DoKho = ?, MaChuong = ? WHERE MaCH = ?";
        try (Connection conn = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getText());
            stmt.setString(2, a.getDifficult().name());
            stmt.setString(3, a.getChapter().getID());
            stmt.setString(4, a.getID());
            answerDAL.deleteByQID(a.getID());
            for (AnswerDTO ans : a.getAns())
                answerDAL.add(ans, a.getID());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại!");
            e.printStackTrace();
        }
        return false;
    }

    //Add
    public Boolean add(QuestionDTO a) {
        String sql = "INSERT INTO cauhoi (MaCH, NoiDung, DoKho, MaChuong, MaND) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            a.setID(getNextId());
            stmt.setString(1, a.getID());
            stmt.setString(2, a.getText());
            stmt.setString(3, a.getDifficult().name());
            stmt.setString(4, a.getChapter().getID());
            stmt.setString(5, a.getCreatedBy());
            stmt.executeUpdate();

            for (AnswerDTO ans : a.getAns())
                answerDAL.add(ans, a.getID());
            return true;
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại!");
            e.printStackTrace();
        }
        return false;
    }

    //Delete
    public Boolean delete(String ID) {
        String sql = "DELETE FROM cauhoi WHERE MaCH = ?";
        try (Connection conn = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ID);
            answerDAL.deleteByQID(ID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại!");
            e.printStackTrace();
        }
        return false;
    }

    public List<QuestionDTO> getByChapter(String ID) {
        ArrayList<QuestionDTO> array = new ArrayList<>();
        String sql = "SELECT * FROM cauhoi WHERE MaChuong = ?";
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
                
            stmt.setString(1, ID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                QuestionDTO ques = new QuestionDTO(rs.getString("MaCH"),
                        chapterDAL.get(rs.getString("MaChuong")),
                        rs.getString("MaND"),
                        Enums.DifficultValue.valueOf(rs.getString("DoKho")),
                        rs.getString("NoiDung"));
                ques.setAns(answerDAL.getAllByQId(ques.getID()));
                ques.setSubject(subjectDAL.getByChapID(ques.getChapter().getID()));
                array.add(ques);
            }
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại!");
            e.printStackTrace();
        }
        return array;
    }

    public List<QuestionDTO> getByDiff(Enums.DifficultValue diff) {
        return getAll().stream().filter(question -> diff == question.getDifficult()).collect(Collectors.toList());
    }

    public List<QuestionDTO> getByDiffChap(String ID, Enums.DifficultValue diff) {
        return getByChapter(ID).stream().filter(question -> diff == question.getDifficult()).collect(Collectors.toList());
    }

    public ArrayList<QuestionDTO> getRandom(int limit, String ChapID, Enums.DifficultValue diff) {
        ArrayList<QuestionDTO> array = null;
        String sql = "SELECT * FROM cauhoi WHERE MaChuong = ? AND DoKho = ? ORDER BY RAND() LIMIT ?";
        try (Connection conn = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ChapID);
            stmt.setString(2, diff.toString());
            stmt.setInt(3, limit);

            array = new ArrayList<QuestionDTO>();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                QuestionDTO ques = new QuestionDTO(rs.getString("MaCH"),
                        chapterDAL.get(rs.getString("MaChuong")),
                        rs.getString("MaND"),
                        Enums.DifficultValue.valueOf(rs.getString("DoKho")),
                        rs.getString("NoiDung"));
                ques.setAns(answerDAL.getAllByQId(ques.getID()));
                ques.setSubject(subjectDAL.getByChapID(ques.getChapter().getID()));
                array.add(ques);
            }
        } catch (SQLException e) {
            System.out.println("Kết nối cauhoi thất bại!");
            e.printStackTrace();
        }
        return array;
    }

    public List<QuestionDTO> search(String keyword, Enums.DifficultValue difficulty, ChapterDTO chapter) {
        List<QuestionDTO> allQuestions = getAll();
        return allQuestions.stream()
                .filter(question -> {
                    boolean keywordMatch = keyword == null || keyword.isEmpty() ||
                            question.getText().toLowerCase().contains(keyword.toLowerCase());
                    boolean difficultyMatch = difficulty == null || question.getDifficult() == difficulty;
                    boolean chapterMatch = chapter == null || question.getChapter().getID().equals(chapter.getID());
                    return keywordMatch && difficultyMatch && chapterMatch;
                })
                .collect(Collectors.toList());
    }
}
