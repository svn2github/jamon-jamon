    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/html");
        HelloTemplate helloTemplate = new HelloTemplate();
        String numString = request.getParameter(NUM_KEY);
        if (numString != null) {
            helloTemplate.setNum(Integer.parseInt(numString));
        }
        helloTemplate.render(response.getWriter());
    }

    public static final String NUM_KEY = "num";