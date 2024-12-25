package com.qst.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.qst.dao.ItemDao;
import com.qst.dao.UserDao;
import com.qst.entity.Item;

public class VoteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 设置请求和响应编码
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		// 获取投票选项参数
		String[] VO_ID = request.getParameterValues("VO_ID");
		System.out.println("投票开始前.......");

		// 获取标题标识
		String vsIdParam = request.getParameter("VS_ID");
		if (vsIdParam == null || vsIdParam.trim().isEmpty()) {
			System.out.println("缺少 VS_ID 参数！");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少必要的参数 VS_ID");
			return;
		}

		int VS_ID;
		try {
			VS_ID = Integer.parseInt(vsIdParam);
		} catch (NumberFormatException e) {
			System.out.println("VS_ID 参数格式错误：" + vsIdParam);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "VS_ID 参数格式错误");
			return;
		}
		System.out.println("标题标识为========== " + VS_ID);

		// 从 session 中得到用户名
		HttpSession session = request.getSession(false); // 使用 false 避免创建新会话
		if (session == null) {
			System.out.println("会话不存在！");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "会话不存在，请重新登录");
			return;
		}

		Object nameObj = session.getAttribute("name");
		if (nameObj == null) {
			System.out.println("Session 中的 'name' 属性为 null！");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录或会话已过期");
			return;
		}

		String VU_USER_NAME = nameObj.toString();
		System.out.println("用户账号为========== " + VU_USER_NAME);

		// 查找用户 ID
		UserDao udao = new UserDao();
		Integer VU_USER_ID = udao.findUserId(VU_USER_NAME);
		if (VU_USER_ID == null) {
			System.out.println("未找到用户名对应的用户 ID！");
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "内部错误：用户 ID 未找到");
			return;
		}
		System.out.println("用户标识为========== " + VU_USER_ID);

		// 检查 VO_ID 是否为 null 或空
		if (VO_ID == null || VO_ID.length == 0) {
			System.out.println("未选择任何投票选项！");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "请选择至少一个投票选项");
			return;
		}

		// 遍历 VO_ID 插入数据
		ItemDao dao = new ItemDao(); // 将 ItemDao 的实例化移到循环外部，提高效率
		for (String c : VO_ID) {
			if (c == null || c.trim().isEmpty()) {
				System.out.println("发现无效的 VO_ID 值：" + c);
				continue; // 跳过无效的 VO_ID
			}

			Item item = new Item();
			try {
				item.setVO_ID(Integer.parseInt(c));
			} catch (NumberFormatException e) {
				System.out.println("VO_ID 参数格式错误：" + c);
				continue; // 跳过格式错误的 VO_ID
			}
			item.setVS_ID(VS_ID);
			item.setVU_USER_ID(VU_USER_ID);
			try {
				dao.addItem(item);
				System.out.println("成功插入投票记录：VO_ID=" + c + ", VS_ID=" + VS_ID + ", VU_USER_ID=" + VU_USER_ID);
			} catch (Exception e) {
				System.out.println("插入投票记录时发生异常：VO_ID=" + c);
				e.printStackTrace();
				// 根据需求决定是否继续或中断循环
			}
		}

		System.out.println("投票成功.......");
		request.setAttribute("msg", "投票成功!");
		request.getRequestDispatcher("/list").forward(request, response);
		// 或者使用重定向
		// response.sendRedirect("list");
	}
}
