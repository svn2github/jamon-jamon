    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/html");
        HelloTemplate helloTemplate = new HelloTemplate();
        helloTemplate.render(response.getWriter(), 10);
    }