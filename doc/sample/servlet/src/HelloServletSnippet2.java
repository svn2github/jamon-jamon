    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/html");
        HelloTemplate helloTemplate = new HelloTemplate();
        String numString = request.getParameter("num");
        if (numString != null) {
            helloTemplate.setNum(Integer.parseInt(numString));
        }
        helloTemplate.render(response.getWriter());
    }