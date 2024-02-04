package com.myapp.office_mp.email;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class ReadEmailTaskJava extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        readEmails();
        return null;
    }

    // Метод для чтения сообщений
    private void readEmails() {
        // Настройте свойства для подключения
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");

        try {
            // Создайте сессию и подключитесь к почтовому ящику
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "sferaved.t@gmail.com", "dgsu dduv euah cqnl");

            // Откройте папку "inbox"
            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            // Обработайте сообщения по вашему желанию
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                try {
                    // Проверка на Subject: CBMSG1
                    if (message.getSubject() != null && message.getSubject().contains("CBMSG1")) {
                        // Выведи в лог различные поля сообщения
                        Log.d("EmailReader", "Subject: " + message.getSubject());
                        Log.d("EmailReader", "From: " + Arrays.toString(message.getFrom()));
                        Log.d("EmailReader", "To: " + Arrays.toString(message.getRecipients(Message.RecipientType.TO)));
                        Log.d("EmailReader", "Date: " + message.getSentDate());

                        // Вывод списка вложений
                        // Вывод списка вложений
                        Multipart multipart = (Multipart) message.getContent();
                        for (int j = 0; j < multipart.getCount(); j++) {
                            BodyPart bodyPart = multipart.getBodyPart(j);
                            if (bodyPart instanceof MimeBodyPart) {
                                MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                                String fileName = mimeBodyPart.getFileName();
                                if (fileName != null) {
                                    Log.d("EmailReader", "Attachment: " + fileName);

                                    // Проверка на файл с расширением ".imfx"
                                    if (fileName.endsWith(".imfx")) {
                                        // Получи содержимое архива
                                        InputStream is = mimeBodyPart.getInputStream();

                                        // Распаковка архива
                                        try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(is)) {
                                            ZipArchiveEntry entry;
                                            while ((entry = zipInputStream.getNextZipEntry()) != null) {
                                                // Выводи имена всех файлов в лог
                                                Log.d("EmailReader", "   Archive Content: " + entry.getName());
                                                if (entry.getName().endsWith("doclist.xml")) {
                                                    doclistParsing (zipInputStream);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        // Дополнительные поля можно вывести по аналогии

                        // Разделитель для удобства чтения лога
                        Log.d("EmailReader", "-----------------------------");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("EmailReader", "Error while reading email: " + e.toString());
                }

            }

            // Закройте папку и соединение
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("EmailReader", "Error: " + e.toString());
        }
    }

    private String formatModificationDate(String modificationDate) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

            Date modificationDateFormatted = inputFormat.parse(modificationDate);
            assert modificationDateFormatted != null;
            return outputFormat.format(modificationDateFormatted);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("EmailReader", "Error formatting ModificationDate: " + e.getMessage());
            return modificationDate; // В случае ошибки, просто возвращаем исходную строку
        }
    }

    private List<Map<String, String>> doclistParsing(ZipArchiveInputStream zipInputStream) throws IOException, SAXException, ParserConfigurationException {
        List<Map<String, String>> resultList = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(zipInputStream);

        // Найти и вывести значения нужных тегов
        NodeList nodeList = doc.getElementsByTagName("DocNumber");
        if (nodeList.getLength() > 0) {
            String docNumber = nodeList.item(0).getTextContent();
            Log.d("EmailReader", "DocNumber: " + docNumber);
            addValueToResultList(resultList, "DocNumber", docNumber);
        }

        nodeList = doc.getElementsByTagName("ModificationDate");
        if (nodeList.getLength() > 0) {
            String modificationDate = nodeList.item(0).getTextContent();
            Log.d("EmailReader", "ModificationDate: " + formatModificationDate(modificationDate));
            addValueToResultList(resultList, "ModificationDate", formatModificationDate(modificationDate));
        }

        nodeList = doc.getElementsByTagName("FileName");
        if (nodeList.getLength() > 0) {
            String fileNameTag = nodeList.item(0).getTextContent();
            Log.d("EmailReader", "FileName: " + fileNameTag);
            addValueToResultList(resultList, "FileName", fileNameTag);
        }

        nodeList = doc.getElementsByTagName("Comment");
        if (nodeList.getLength() > 0) {
            String comment = nodeList.item(0).getTextContent();
            Log.d("EmailReader", "Comment: " + comment);
            addValueToResultList(resultList, "Comment", comment);
        }
        Log.d("EmailReader", "Result List: " + resultList.toString());
        return resultList;
    }

    private void addValueToResultList(List<Map<String, String>> resultList, String key, String value) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(key, value);
        resultList.add(resultMap);
    }


}
