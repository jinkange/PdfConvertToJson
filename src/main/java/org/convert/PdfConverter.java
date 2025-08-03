package org.convert;

import java.io.*;


public class PdfConverter {

    public static String convertPdfToJson(File pdfFile) throws IOException {
        extractExecutable("pdf2json.exe");  // JAR 내부 리소스 기준

        if (!pdfFile.exists()) {
            throw new FileNotFoundException("PDF 파일이 존재하지 않습니다: " + pdfFile.getAbsolutePath());
        }

        // 실행파일 경로 설정
        String exePath = getExecutablePath();
        System.out.println("[PdfConverter] " + exePath);
        // 파일 이름 및 경로
        String pdfFilePath = pdfFile.getAbsolutePath();
        System.out.println("[PdfConverter] " + pdfFilePath);
        String jsonFilePath = getJsonPathFromPdf(pdfFilePath);
        System.out.println("[PdfConverter] " + jsonFilePath);
        // 실행 명령어 구성
        ProcessBuilder builder = new ProcessBuilder(exePath, pdfFilePath);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        // 로그 출력 (디버깅용)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[PY] " + line);
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python 실행파일 종료 코드: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Python 실행파일 대기 중 인터럽트 발생", e);
        }

        // JSON 파일 읽기
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new FileNotFoundException("생성된 JSON 파일이 존재하지 않습니다: " + jsonFilePath);
        }

        return readFileToString(jsonFile);
    }

//    private static String getExecutablePath() {
//        // 리소스 폴더에 있는 실행파일 경로를 OS에 맞게 반환
//        String os = System.getProperty("os.name").toLowerCase();
//        String exeName = os.contains("win") ? "pdf2json.exe" : "./pdf2json";  // Linux나 macOS의 경우
//        File exeFile = new File("src/main/resources/" + exeName); // 개발 중 위치
//        if (!exeFile.exists()) {
//            throw new RuntimeException("실행파일이 존재하지 않습니다: " + exeFile.getAbsolutePath());
//        }
//        return exeFile.getAbsolutePath();
//    }
    private static String getExecutablePath() {
        String resourcePath;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            resourcePath = "pdf2json.exe";
        } else {
            resourcePath = "pdf2json"; // 리눅스용 실행파일
        }

        try (InputStream in = PdfConverter.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("리소스에서 실행파일을 찾을 수 없습니다: " + resourcePath);
            }

            File tempExe = File.createTempFile("pdf2json", os.contains("win") ? ".exe" : "");
            tempExe.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempExe)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            if (!os.contains("win")) {
                // 리눅스, 맥일 경우 실행권한 추가
                tempExe.setExecutable(true);
            }

            return tempExe.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException("실행파일 복사 중 오류 발생", e);
        }
    }

    private static String getJsonPathFromPdf(String pdfPath) {
        return pdfPath.replaceAll("(?i)\\.pdf$", ".json");
    }

    private static String readFileToString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static String extractExecutable(String resourcePath) throws IOException {
        InputStream in = PdfConverter.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new FileNotFoundException("리소스를 찾을 수 없습니다: " + resourcePath);
        }

        File tempFile = File.createTempFile("pdf2json", ".exe");
        tempFile.deleteOnExit();

        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return tempFile.getAbsolutePath();
    }
}
