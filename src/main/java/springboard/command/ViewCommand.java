package springboard.command;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import springboard.model.JDBCTemplateDAO;
import springboard.model.SpringBbsDTO;

public class ViewCommand implements BbsCommandImpl {
	
	@Override
	public void execute(Model model) {
		
		//요청 일괄 받기 
		Map<String, Object> paramMap = model.asMap();
		HttpServletRequest req = (HttpServletRequest)paramMap.get("req");
		
		//폼값받기 
		String idx = req.getParameter("idx");
		String nowPage = req.getParameter("nowPage");
		
		//DAO, DTO 객체 생성 및 상세보기 처리를 위한 메서드 호출 
		JDBCTemplateDAO dao = new JDBCTemplateDAO();
		SpringBbsDTO dto = new SpringBbsDTO();
		dto = dao.view(idx);
		dto.setContents(dto.getContents().replace("\r\n", "<br/>"));
		
		model.addAttribute("viewRow", dto);
		model.addAttribute("nowPage", nowPage);
	}
}
