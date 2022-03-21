package springboard.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import springboard.command.BbsCommandImpl;
import springboard.command.DeleteActionCommand;
import springboard.command.EditActionCommand;
import springboard.command.EditCommand;
import springboard.command.ListCommand;
import springboard.command.ViewCommand;
import springboard.command.WriteActionCommand;
import springboard.model.JDBCTemplateDAO;
import springboard.model.JdbcTemplateConst;
import springboard.model.SpringBbsDTO;

/*
기본패키지로 설정한 곳에 컨트롤러를 선언하면 요청이 들어왔을때
Auto scan 된다. 해당 설정은 servlet-context.xml에서 추가한다.
 */
@Controller
public class BbsController {
	
	/*
	@Autowired
		: 스프링 설정파일에서 이미 생성된 빈을 자동으로 주입받고
		싶을때 사용한다. 타입을 기반으로 자동주입되며, 만약 해당 타입의
		빈이 존재하지 않으면 에러가 발생되어 서버를 시작할 수 없다.
		-생성자, 멤버변수, 메서드(setter)에 적용할 수 있다.
		-타입을 이용해 자동으로 프로퍼티 값을 설정한다.
		-해당 어노테이션은 멤버변수에만 적용할 수 있다. 메서드내의
		지역변수에는 사용할 수 없다.
		-타입을 통해 자동으로 설정되므로 같은 타입이 2개 이상 존재하면
		예외가 발생한다. 
	*/
	private JdbcTemplate template;
	
	/*
	servlet-context.xml에서 생성한 빈을 여기서 자동으로 주입받는다.
	해당 빈은 스프링 컨테이너가 시작될때 생성되며, 타입을 기반으로 
	자동 주입 받게된다.
	 */
	@Autowired
	public void setTemplate(JdbcTemplate template) {
		this.template = template;
		System.out.println("@Autowired -> JdbcTemplate 연결성공");
		
		//JdbcTemplate을 해당 프로그램 전체에서 사용하기 위한 설정.(static 타입)
		JdbcTemplateConst.template = this.template;
	}
	
	/*
	멤버변수로 선언하여 클래스에서 전역적으로 사용할 수 있다. 해당
	클래스의 모든 Command(서비스)객체는 해당 인터페이스를 구현하여 정의한다. 
	*/
	BbsCommandImpl command = null;
	
	@RequestMapping("/board/list.do")
	public String list(Model model, HttpServletRequest req) {
		
		/*
		사용자로부터 받은 모든 요청은 request객체에 저장되고, 이를
		ListCommand객체로 전달하기 위해 Model객체에 저장한 후 매개변수로
		전달한다. 
		*/
		model.addAttribute("req", req); //request객체 자체를 Model에 저장
		command = new ListCommand(); //service역할의 ListCommand객체 생성
		command.execute(model); //해당 객체로 Model객체 자체를 전달 
		
		return "07Board/list";
	}
	
	//글쓰기 페이지로 진입하기 위한 매핑 처리 
	@RequestMapping("/board/write.do")
	public String write(Model model) {
		return "07Board/write";
	}
	
	//전송방식이 post이므로 value, method까지 같이 기술해서 매핑 
	@RequestMapping(value="/board/writeAction.do", method=RequestMethod.POST) 
	public String writeAction(Model model, HttpServletRequest req,
			SpringBbsDTO springBbsDTO) {
		
		/*
		글쓰기 페이지에서 전송된 모든 폼값은 SpringBbsDTO 객체를 통해
		한번에 받을 수 있다. Spring에서는 커맨드객체를 통해 이와같은
		처리를 할 수 있다. 
		*/
		//request객체와 함께 Model에 저장
		model.addAttribute("req", req);
		model.addAttribute("springBbsDTO",springBbsDTO);
		//요청을 전달할 service객체를 생성한 후 execute()메서드 호출
		command = new WriteActionCommand();
		command.execute(model);
		
		//뷰를 반환하지 않고, 지정된 URL(요청명)로 이동한다. 
		return "redirect:list.do?nowPage=1";
	}
	
	//글 내용보기
	@RequestMapping("/board/view.do")
	public String view(Model model, HttpServletRequest req) {
		
		//사용자의 요청을 저장한 request객체를 Model객체에 저장한 후 전달한다.
		model.addAttribute("req", req); 
		command = new ViewCommand(); 
		command.execute(model); 
		
		return "07Board/view";
	}
	
	//패스워드 검증 페이지 진입 
	@RequestMapping("/board/password.do")
	public String password(Model model, HttpServletRequest req) {
		
		//일련번호는 컨트롤러에서 파라미터를 받은 후 Model에 저장하여 View로 전달한다.  
		model.addAttribute("idx", req.getParameter("idx"));
		
		return "07Board/password";
	}
	
	//패스워드 검증
	@RequestMapping("/board/passwordAction.do")
	public String passwordAction(Model model, HttpServletRequest req) {
		
		String modePage = null;
		//폼값받기 
		String mode = req.getParameter("mode");
		String idx = req.getParameter("idx");
		String nowPage = req.getParameter("nowPage");
		String pass = req.getParameter("pass");
		
		//DAO에서 일련번호와 패스워드를 통해 검증
		JDBCTemplateDAO dao = new JDBCTemplateDAO();
		int rowExist = dao.password(idx, pass);
		
		if(rowExist<=0) {
			//패스워드 검증실패시에는 이전페이지로 돌아간다. 
			model.addAttribute("isCorrMsg", "패스워드가 일치하지 않습니다.");
			model.addAttribute("idx", idx);
			
			//패스워드 검증 페이지를 반환한다.
			modePage = "07Board/password";
		}
		
		else {
			//검증에 성공한 경우 수정 혹은 삭제 처리를 한다. 
			System.out.println("검증완료");
			
			if(mode.equals("edit")) {
				/*
				mode가 수정인 경우 수정페이지로 이동한다.
				 */
				model.addAttribute("req", req);
				command = new EditCommand();
				command.execute(model);
				
				modePage = "07Board/edit";
			}
			else if(mode.equals("delete")) {
				//mode가 delete인경우 즉시 삭제 처리
				model.addAttribute("req", req);
				command = new DeleteActionCommand();
				command.execute(model);
				
				//삭제 후에는 리스트 페이지로 이동한다. 
				model.addAttribute("nowPage", req.getParameter("nowPage"));
				modePage = "redirect:list.do";
			}
		}
		
		return modePage;
	}
	
	//수정처리
	@RequestMapping("/board/editAction.do")
	public String editAction(HttpServletRequest req, Model model, SpringBbsDTO springBbsDTO) {
		
		/*
		request내장객체와 수정페잊에서 전송한 모든 폼값을 저장한 DTO객체를
		Model에 저장한 후 서비스 객체로 전달한다. 
		 */
		model.addAttribute("req",req);
		model.addAttribute("springBbsDTO",springBbsDTO);
		command = new EditActionCommand();
		command.execute(model);
		
		/*
		수정처리가 완료되면 상세페이지로 이동하게 되는데 이때 idx와 같은
		파라미터가 필요하다. Model객체에 저장한 후 redirect하면 자동으로
		쿼리스트링 형태로 만들어준다. 
		*/
		model.addAttribute("idx", req.getParameter("idx"));
		model.addAttribute("nowPage", req.getParameter("nowPage"));
		
		return "redirect:view.do";
	}

}
