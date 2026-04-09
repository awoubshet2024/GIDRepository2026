package com.alem.GIA.service;

import com.alem.GIA.entity.Payment;
import com.alem.GIA.repository.PaymentRepository;
import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
//import java.awt.*;
import com.lowagie.text.Image;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
@Service
public class InvoiceService {
    private final PaymentRepository paymentRepository;

    public InvoiceService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public ByteArrayOutputStream generateInvoice(Integer paymentId) throws Exception {
        Payment payment = paymentRepository.findByIdWithMember(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Generate invoice number if not exists
        if (payment.getInvoiceNumber() == null) {
            //payment.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setInvoiceNumber("INV-" + System.currentTimeMillis());
            payment.setInvoiceIssued(true);
            paymentRepository.save(payment);
        }

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        addHeader(document);
        addPaidBadge(document);
        addInvoiceInfo(document, payment);
        addMemberInfo(document, payment);
        addPaymentTable(document, payment);
        addFooter(document);

        document.close();

        return out;
    }


   private void addHeader(Document document) throws Exception {

       PdfPTable headerTable = new PdfPTable(2);
       headerTable.setWidthPercentage(100);
       headerTable.setWidths(new int[]{1,2});


       Image logo = Image.getInstance(
               getClass().getResource("/static/images/GIDIRLogo.jpg")
       );
       logo.scaleToFit(120,120);

       PdfPCell logoCell = new PdfPCell(logo);
       logoCell.setBorder(Rectangle.NO_BORDER);
       headerTable.addCell(logoCell);

       // COMPANY INFO
       Font companyFont = new Font(Font.HELVETICA, 14, Font.BOLD);
       Font infoFont = new Font(Font.HELVETICA, 10);

       Paragraph companyInfo = new Paragraph();
       companyInfo.add(new Phrase("BISRATE GEBRIEL IDIR\n", companyFont));
       companyInfo.add(new Phrase("3518 Clarkston Industrial Blvd\n", infoFont));
       companyInfo.add(new Phrase("Clarkston, GA 30021\n", infoFont));
       companyInfo.add(new Phrase("Phone: (404) 506-9064\n", infoFont));
       companyInfo.add(new Phrase("Email: info@gia.org\n", infoFont));

       PdfPCell infoCell = new PdfPCell(companyInfo);
       infoCell.setBorder(Rectangle.NO_BORDER);
       infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

       headerTable.addCell(infoCell);

       document.add(headerTable);

       document.add(new Paragraph(" "));

       // INVOICE TITLE
       Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);

       Paragraph title = new Paragraph("INVOICE", titleFont);
       title.setAlignment(Element.ALIGN_RIGHT);

       document.add(title);

       document.add(new Paragraph(" "));
       LineSeparator line = new LineSeparator();
       document.add(line);
       document.add(new Paragraph(" "));
       //document.add(line);

   }

    private void addInvoiceInfo(Document document, Payment payment) throws Exception {

       // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1,2});
        String date = payment.getPaymentDate() != null
                ? payment.getPaymentDate().format(formatter)
                : "N/A";

        table.addCell(getCell("Invoice Number:", true));
        table.addCell(getCell(payment.getInvoiceNumber(), false));

        table.addCell(getCell("Payment Date:", true));
        table.addCell(getCell( date,false));

        table.addCell(getCell("Confirmation#:", true));
        table.addCell(getCell(
                payment.getStripePaymentIntentId() != null
                        ? payment.getStripePaymentIntentId()
                        : "Manual Payment",
                false));
        table.addCell(getHeaderCell("Billing Period"));
        table.addCell(getCell(payment.getBillingPeriod().name(), false));

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addMemberInfo(Document document, Payment payment) throws Exception {

        document.add(new Paragraph("Bill To:", new Font(Font.HELVETICA, 12, Font.BOLD)));

        document.add(new Paragraph(
                payment.getMember().getFirstName() + " " +
                        payment.getMember().getLastName()
        ));

        document.add(new Paragraph(payment.getMember().getEmail()));
        document.add(new Paragraph(" "));
    }


    private void addPaymentTable(Document document, Payment payment) throws Exception {

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        table.addCell(getHeaderCell("Description"));
        table.addCell(getHeaderCell("Status"));
        table.addCell(getHeaderCell("Amount"));

        table.addCell(getCell(payment.getReason(), false));
        table.addCell(getCell(payment.getStatus(), false));
        table.addCell(getCell(String.format("$%.2f" , payment.getAmount()), false));

        document.add(table);

        document.add(new Paragraph(" "));

        addPaymentSummary(document, payment);
    }
    private void addPaymentSummary(Document document, Payment payment) throws Exception {

        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(40);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);

        summary.addCell(getCell("Total:", true));
        summary.addCell(getCell(String.format("$%.2f", payment.getAmount()), false));

        summary.addCell(getCell("Payment Method:", true));
        summary.addCell(getCell(getPaymentMethod(payment), false));

        document.add(summary);
    }
    private String getPaymentMethod(Payment payment) {

        if (payment.getPaymentMethod() == null) {
            return "Unknown";
        }

        switch (payment.getPaymentMethod()) {

            case CARD:
                if (payment.getCardLast4() != null) {
                    return "Card •••• " + payment.getCardLast4();
                }
                return "Card";

            case CASH:
                return "Cash";

            case CHECK:
                if (payment.getCheckNumber() != null) {
                    return "Check #" + payment.getCheckNumber();
                }
                return "Check";

            default:
                return payment.getPaymentMethod().name();
        }
    }
    private void addPaidBadge(Document document) throws Exception {

        Font badgeFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);

        PdfPCell cell = new PdfPCell(new Phrase("PAID", badgeFont));
        cell.setBackgroundColor(new Color(46, 204, 113));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(10);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        document.add(table);
    }
    private void addFooter(Document document) throws Exception {
        Paragraph footer1 = new Paragraph(
                "BISRATE GEBRIEL IDIR | www.gia.org | (404) 506-9064",
                new Font(Font.HELVETICA, 9)
        );
        footer1.setAlignment(Element.ALIGN_CENTER);
        document.add(footer1);

        Paragraph footer = new Paragraph(

                "Thank you for your payment.",
                new Font(Font.HELVETICA, 10, Font.ITALIC)
        );

        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private PdfPCell getCell(String text, boolean bold) {
        Font font = bold
                ? new Font(Font.HELVETICA, 12, Font.BOLD)
                : new Font(Font.HELVETICA, 12);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell getHeaderCell(String text) {
        Font font = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.DARK_GRAY);
        return cell;
    }
}