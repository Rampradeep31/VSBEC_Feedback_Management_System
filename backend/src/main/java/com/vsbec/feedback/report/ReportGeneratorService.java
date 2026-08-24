package com.vsbec.feedback.report;

import com.vsbec.feedback.entity.*;
import com.vsbec.feedback.exception.ApiException;
import com.vsbec.feedback.repository.FeedbackAnswerRepository;
import com.vsbec.feedback.repository.FeedbackQuestionRepository;
import com.vsbec.feedback.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the "Students' Feedback on Course Delivery" DOCX report, replicating
 * the layout of the original VSBEC manual report exactly:
 * page 1 = Theory subjects table + references + Q1-10 legend + signatures,
 * page 2 = Lab subjects table + references + Q1-10 legend + signatures.
 *
 * Font: Times New Roman throughout. Page: US Letter, narrow margins, matching
 * the source document's section properties.
 */
@Service
@RequiredArgsConstructor
public class ReportGeneratorService {

    private final SubjectRepository subjectRepository;
    private final FeedbackQuestionRepository questionRepository;
    private final FeedbackAnswerRepository answerRepository;

    @Value("${app.reports.output-dir}")
    private String outputDir;

    @Value("${app.reports.college-name}")
    private String collegeName;

    @Value("${app.reports.report-title}")
    private String reportTitle;

    @Value("${app.reports.form-no}")
    private String formNo;

    @Value("${app.reports.effective-date}")
    private String effectiveDate;

    @Value("${app.reports.logo-path:./assets/vsbec-logo.png}")
    private String logoPath;

    @Value("${app.scoring.percentage-multiplier:20}")
    private int percentageMultiplier;


    private static final String FONT = "Times New Roman";

    public record SubjectReportRow(Subject subject, Map<Integer, Double> questionPercentages, double totalPercentage) {}

    @Transactional(readOnly = true)
    public Path generateDocx(ClassGroup classGroup) throws IOException {
        List<Subject> theorySubjects = subjectRepository
                .findByClassGroup_IdAndSubjectTypeOrderByDisplayOrderAsc(classGroup.getId(), SubjectType.THEORY);
        List<Subject> labSubjects = subjectRepository
                .findByClassGroup_IdAndSubjectTypeOrderByDisplayOrderAsc(classGroup.getId(), SubjectType.LAB);

        if (theorySubjects.isEmpty() && labSubjects.isEmpty()) {
            throw ApiException.badRequest("No subjects configured for this class");
        }

        try (XWPFDocument doc = new XWPFDocument()) {
            setPageLayout(doc);

            if (!theorySubjects.isEmpty()) {
                buildReportPage(doc, classGroup, theorySubjects, SubjectType.THEORY);
            }
            if (!labSubjects.isEmpty()) {
                if (!theorySubjects.isEmpty()) {
                    addPageBreak(doc);
                }
                buildReportPage(doc, classGroup, labSubjects, SubjectType.LAB);
            }

            Files.createDirectories(Path.of(outputDir));
            String filename = "Feedback_Report_" + classGroup.getClassLabel().replaceAll("\\s+", "_")
                    + "_" + System.currentTimeMillis() + ".docx";
            Path outPath = Path.of(outputDir, filename);
            try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
                doc.write(fos);
            }
            return outPath;
        }
    }

    // ---------------------------------------------------------------------

    private void setPageLayout(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().isSetSectPr()
                ? doc.getDocument().getBody().getSectPr()
                : doc.getDocument().getBody().addNewSectPr();

        CTPageSz pageSz = sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
        pageSz.setW(java.math.BigInteger.valueOf(12240)); // US Letter width (DXA)
        pageSz.setH(java.math.BigInteger.valueOf(15840)); // US Letter height (DXA)

        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();
        pageMar.setLeft(java.math.BigInteger.valueOf(720));
        pageMar.setRight(java.math.BigInteger.valueOf(720));
        pageMar.setTop(java.math.BigInteger.valueOf(432));
        pageMar.setBottom(java.math.BigInteger.valueOf(432));
        pageMar.setHeader(java.math.BigInteger.valueOf(0));
        pageMar.setFooter(java.math.BigInteger.valueOf(0));
        pageMar.setGutter(java.math.BigInteger.valueOf(0));
    }

    private void addPageBreak(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.addBreak(BreakType.PAGE);
    }

    private void buildReportPage(XWPFDocument doc, ClassGroup cg, List<Subject> subjects, SubjectType type) {
        // ---- Top-right corner label ----
        XWPFParagraph corner = doc.createParagraph();
        corner.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun cornerRun = corner.createRun();
        cornerRun.setText("VSBEC");
        cornerRun.setFontFamily(FONT);
        cornerRun.setFontSize(9);

        // ---- Logo Crest ----
        try {
            if (logoPath != null) {
                java.nio.file.Path path = java.nio.file.Path.of(logoPath);
                if (java.nio.file.Files.exists(path)) {
                    XWPFParagraph logoP = doc.createParagraph();
                    logoP.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun logoRun = logoP.createRun();
                    int pictureType = path.getFileName().toString().toLowerCase().endsWith(".png")
                            ? XWPFDocument.PICTURE_TYPE_PNG
                            : XWPFDocument.PICTURE_TYPE_JPEG;
                    try (var is = java.nio.file.Files.newInputStream(path)) {
                        logoRun.addPicture(is, pictureType, path.getFileName().toString(), org.apache.poi.util.Units.toEMU(60), org.apache.poi.util.Units.toEMU(60));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        // ---- College name ----
        XWPFParagraph collegeP = doc.createParagraph();
        collegeP.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun collegeRun = collegeP.createRun();
        collegeRun.setText(collegeName);

        collegeRun.setBold(true);
        collegeRun.setUnderline(UnderlinePatterns.SINGLE);
        collegeRun.setFontFamily(FONT);
        collegeRun.setFontSize(13);

        // ---- Report title ----
        XWPFParagraph titleP = doc.createParagraph();
        titleP.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleP.createRun();
        titleRun.setText(reportTitle);
        titleRun.setBold(true);
        titleRun.setUnderline(UnderlinePatterns.SINGLE);
        titleRun.setFontFamily(FONT);
        titleRun.setFontSize(13);

        // ---- Info line: Academic Year / Branch / Semester / Year ----
        XWPFParagraph infoP = doc.createParagraph();
        infoP.setAlignment(ParagraphAlignment.LEFT);
        infoP.setBorderBottom(Borders.SINGLE);
        addInfoRun(infoP, "Academic Year: ", cg.getAcademicYear().getYearLabel(), true);
        addInfoRun(infoP, "     Branch: ", cg.getDepartment().getName(), true);
        addInfoRun(infoP, "     Semester: ", cg.getSemester().name(), true);
        addInfoRun(infoP, "     Year: ", cg.getYearOfStudy().name() + "-" + cg.getSection(), false);

        doc.createParagraph(); // spacer

        // ---- Faculty / subject table ----
        List<FeedbackQuestion> questions = questionRepository
                .findBySubjectTypeAndActiveTrueOrderByQuestionNumberAsc(type);
        Map<Long, SubjectReportRow> rows = new LinkedHashMap<>();
        for (Subject s : subjects) {
            rows.put(s.getId(), computeSubjectRow(s, questions));
        }

        buildFeedbackTable(doc, subjects, rows, questions);

        doc.createParagraph(); // spacer

        // ---- References ----
        XWPFParagraph refHeading = doc.createParagraph();
        XWPFRun refHeadingRun = refHeading.createRun();
        refHeadingRun.setText("References");
        refHeadingRun.setBold(true);
        refHeadingRun.setFontFamily(FONT);
        refHeadingRun.setFontSize(11);

        int i = 1;
        for (Subject s : subjects) {
            XWPFParagraph refP = doc.createParagraph();
            refP.setIndentationLeft(400);
            XWPFRun refRun = refP.createRun();
            refRun.setText(i + ". " + s.getFaculty().getName());
            refRun.setFontFamily(FONT);
            refRun.setFontSize(10);
            i++;
        }

        doc.createParagraph(); // spacer

        // ---- Question legend ----
        for (FeedbackQuestion q : questions) {
            XWPFParagraph qP = doc.createParagraph();
            XWPFRun qLabel = qP.createRun();
            qLabel.setText("Question " + q.getQuestionNumber() + ": ");
            qLabel.setBold(true);
            qLabel.setFontFamily(FONT);
            qLabel.setFontSize(10);

            XWPFRun qText = qP.createRun();
            qText.setText(q.getQuestionText());
            qText.setFontFamily(FONT);
            qText.setFontSize(10);
        }

        doc.createParagraph(); // spacer
        doc.createParagraph(); // spacer

        // ---- Signature line ----
        XWPFParagraph sigP = doc.createParagraph();
        addBoldRun(sigP, "Class Advisor Signature", 11, 3);
        addBoldRun(sigP, "HOD Signature", 11, 2);
        addBoldRun(sigP, "Principal Signature", 11, 0);

        doc.createParagraph(); // spacer

        // ---- Footer block ----
        XWPFParagraph footerP1 = doc.createParagraph();
        addBoldRun(footerP1, formNo, 9, 4);
        addBoldRun(footerP1, "Effective Date: " + effectiveDate, 9, 0);

        XWPFParagraph footerP2 = doc.createParagraph();
        footerP2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun footerRun2 = footerP2.createRun();
        footerRun2.setBold(true);
        footerRun2.setFontFamily(FONT);
        footerRun2.setFontSize(9);
        footerRun2.setText("© Copyright VSB/" + cg.getDepartment().getShortCode() + "/BATCH " + cg.getAcademicYear().getYearLabel());
    }

    private void addBoldRun(XWPFParagraph p, String text, int fontSize, int trailingTabs) {
        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setFontFamily(FONT);
        run.setFontSize(fontSize);
        run.setText(text);
        for (int i = 0; i < trailingTabs; i++) {
            run.addTab();
        }
    }

    private void addInfoRun(XWPFParagraph p, String label, String value, boolean bold) {
        XWPFRun labelRun = p.createRun();
        labelRun.setFontFamily(FONT);
        labelRun.setFontSize(10);
        labelRun.setText(label);

        XWPFRun valueRun = p.createRun();
        valueRun.setBold(true);
        valueRun.setFontFamily(FONT);
        valueRun.setFontSize(10);
        valueRun.setText(value);
    }

    private SubjectReportRow computeSubjectRow(Subject subject, List<FeedbackQuestion> questions) {
        List<FeedbackAnswer> answers = answerRepository.findBySubject_Id(subject.getId());

        Map<Integer, Double> percentages = new LinkedHashMap<>();
        for (FeedbackQuestion q : questions) {
            double avgRating = answers.stream()
                    .filter(a -> a.getQuestion().getId().equals(q.getId()))
                    .mapToInt(FeedbackAnswer::getRating)
                    .average()
                    .orElse(0.0);
            double percentage = round1(avgRating * percentageMultiplier);
            percentages.put(q.getQuestionNumber(), percentage);
        }

        double total = round1(percentages.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        return new SubjectReportRow(subject, percentages, total);
    }

    private double round1(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String fmt(double val) {
        // Mirrors the source report: whole numbers print without a decimal (e.g. "78" not "78.0")
        if (val == Math.floor(val)) return String.valueOf((int) val);
        return String.valueOf(val);
    }

    private void buildFeedbackTable(XWPFDocument doc, List<Subject> subjects,
                                     Map<Long, SubjectReportRow> rows, List<FeedbackQuestion> questions) {
        XWPFTable table = doc.createTable(subjects.size() + 1, 13);
        table.setWidth("100%");

        int[] colWidthsTwips = {1600, 1900, 620, 620, 620, 620, 620, 620, 620, 620, 620, 620, 700};
        setTableColumnWidths(table, colWidthsTwips);

        String[] headers = {"Staff Name", "Subject", "Que 1", "Que 2", "Que 3", "Que 4", "Que 5",
                "Que 6", "Que 7", "Que 8", "Que 9", "Que 10", "Total"};

        XWPFTableRow headerRow = table.getRow(0);
        for (int c = 0; c < headers.length; c++) {
            XWPFTableCell cell = headerRow.getCell(c);
            setCellText(cell, headers[c], true, ParagraphAlignment.CENTER);
            shadeCell(cell, "D9D9D9");
        }

        int r = 1;
        for (Subject s : subjects) {
            SubjectReportRow row = rows.get(s.getId());
            XWPFTableRow tr = table.getRow(r);

            setCellText(tr.getCell(0), s.getFaculty().getName(), false, ParagraphAlignment.LEFT);
            setCellText(tr.getCell(1), s.getSubjectName(), false, ParagraphAlignment.LEFT);

            for (int qn = 1; qn <= questions.size(); qn++) {
                double pct = row.questionPercentages().getOrDefault(qn, 0.0);
                setCellText(tr.getCell(1 + qn), fmt(pct), false, ParagraphAlignment.CENTER);
            }
            setCellText(tr.getCell(12), fmt(row.totalPercentage()), true, ParagraphAlignment.CENTER);
            r++;
        }
    }

    private void setTableColumnWidths(XWPFTable table, int[] widthsTwips) {
        CTTblGrid grid = table.getCTTbl().getTblGrid();
        if (grid == null) {
            grid = table.getCTTbl().addNewTblGrid();
        }
        List<CTTblGridCol> existingCols = grid.getGridColList();
        for (int i = 0; i < widthsTwips.length; i++) {
            CTTblGridCol col = i < existingCols.size() ? existingCols.get(i) : grid.addNewGridCol();
            col.setW(java.math.BigInteger.valueOf(widthsTwips[i]));
        }
        for (XWPFTableRow row : table.getRows()) {
            for (int c = 0; c < widthsTwips.length; c++) {
                XWPFTableCell cell = row.getCell(c);
                if (cell != null) {
                    cell.setWidth(String.valueOf(widthsTwips[c]));
                }
            }
        }
    }

    private void setCellText(XWPFTableCell cell, String text, boolean bold, ParagraphAlignment align) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(align);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontFamily(FONT);
        run.setFontSize(9);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
    }

    private void shadeCell(XWPFTableCell cell, String hexColor) {
        var tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        var shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
        shd.setFill(hexColor);
    }
}
