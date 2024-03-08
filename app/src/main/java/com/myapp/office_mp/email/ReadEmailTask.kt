package com.myapp.office_mp.email

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.BodyPart
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.MimeBodyPart
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class ReadEmailTask(
    private val context: Context,
    private val accessCode: String
) {
    suspend fun execute(): List<EmailData> {
        return withContext(Dispatchers.IO) {
            readEmails(context)
        }
    }
    private fun readEmails(context:Context): List<EmailData> {
        val resultList = mutableListOf<EmailData>()

        try {
            val message = "Поиск новых сообщений ..."
            Handler(context.mainLooper).post {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            // Настройте свойства для подключения
            val props = Properties()
            props.setProperty("mail.store.protocol", "imaps")

            // Создайте сессию и подключитесь к почтовому ящику
            val session: Session = Session.getDefaultInstance(props, null)
            val store: Store = session.store

            when (this.accessCode) {
                "777" -> store.connect("imap.ukr.net", "sferved.t@ukr.net", "ImVXWwHuw83Q5m16") //Таня
                "321" -> store.connect("imap.ukr.net", "sferved.m@ukr.net", "JMhTvEgCF9GsIyAQ") //Маня
                "456" -> store.connect("imap.ukr.net", "sferved.n@ukr.net", "zyiYFd7LigTv2vyB") //Наташа
            }

//            // Откройте папку "inbox"
            val inbox: Folder = store.getFolder("Inbox")

            inbox.open(Folder.READ_ONLY)
            Log.d("TAG", "readEmails: " + inbox.messageCount)
            if (inbox.messageCount == 0) {
                // Ваш код обработки случая пустого ящика
                val message = "Нет новых сообщений"
                Handler(context.mainLooper).post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                val messages: Array<Message> = inbox.messages
                val unreadMessages = messages.filter { !it.isSet(Flags.Flag.SEEN) }
                // Ваш код дальнейшей обработки непрочитанных сообщений
                for (message in unreadMessages) {
                    // Проверка на Subject: CBMSG1
                    if (message.subject != null && message.subject.contains("CBMS")) {
//                if (message.subject != null) {
                        // Вывести в лог различные поля сообщения
                        Log.d("EmailReader", "Subject: ${message.subject}")
                        Log.d("EmailReader", "From: ${message.from?.contentToString()}")
                        Log.d("EmailReader", "To: ${message.getRecipients(Message.RecipientType.TO)?.contentToString()}")
                        Log.d("EmailReader", "Date: ${message.sentDate}")

                        // Вывод списка вложений
                        val multipart: Multipart = message.content as Multipart
                        for (j in 0 until multipart.count) {
                            val bodyPart: BodyPart = multipart.getBodyPart(j)
                            if (bodyPart is MimeBodyPart) {
                                val mimeBodyPart: MimeBodyPart = bodyPart
                                val fileName: String? = mimeBodyPart.fileName
                                if (fileName != null) {
                                    Log.d("EmailReader_Att", "Attachment: $fileName")
                                    // Проверка на файл с расширением ".imfx"
                                    if (fileName.endsWith(".imfx")) {
                                        val `is`: InputStream = mimeBodyPart.inputStream
                                        val zipInputStream = ZipArchiveInputStream(`is`)
                                        var entry: ZipArchiveEntry? = zipInputStream.nextZipEntry

                                        while (entry != null) {
                                            // Проверяем, что файл имеет расширение .xml
                                            Log.d("TAG_imfx", "readEmails: ${entry.name}")

                                            if (entry.name.endsWith("doc1.xml")) {
                                                val byteArrayInputStream = streamFile (zipInputStream)
                                                doc1Parsing(
                                                    byteArrayInputStream,
                                                    resultList,
                                                    message.subject
                                                )
                                                byteArrayInputStream.close()
                                            }
                                            if (entry.name.endsWith("doc2.xml")) { // Проверяем, что файл имеет расширение .xml
                                                val byteArrayInputStream = streamFile (zipInputStream)
                                                doc2Parsing(
                                                    byteArrayInputStream,
                                                    resultList,
                                                    message.subject)
                                                byteArrayInputStream.close()
                                            }
                                            if (entry.name.endsWith("doclist.xml")) { // Проверяем, что файл имеет расширение .xml
                                                val byteArrayInputStream = streamFile (zipInputStream)
                                                docListParsing(
                                                    byteArrayInputStream,
                                                    resultList,
                                                    message.subject)
                                                byteArrayInputStream.close()
                                            }
                                            // Закрываем byteArrayInputStream после обработки содержимого файла

                                            // Переходим к следующему файлу в архиве
                                            entry = zipInputStream.nextZipEntry
                                        }


                                        zipInputStream.close()

                                    }


                                }
                            }
                        }

                        // Дополнительные поля можно вывести по аналогии
                        // Разделитель для удобства чтения лога
                        Log.d("EmailReader", "-----------------------------")
                    }
                }
            }

            // Обработайте сообщения

            // Закрыть папку и соединение
            inbox.close(false)
            store.close()
            if (resultList.isEmpty()) {
                val message = "Нет новых сообщений"
                Handler(context.mainLooper).post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("EmailReader", "Error: $e")
        }


        return resultList


    }


    private fun doc1Parsing(
        zipInputStream: InputStream?,
        resultList: MutableList<EmailData>,
        subject: String
    ) {
        zipInputStream?.use { inputStream ->
            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(inputStream)
            // Найти и вывести значения нужных тегов
            with(doc) {
                var currentEmailData: EmailData? = null
                var docNumber = ""
                getElementsByTagName("DocNumber").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                    Log.d("EmailReader", "DocNumber: $docNumber")
                }

                var modificationDate = ""
                getElementsByTagName("ActionDate").takeIf { it.length > 0 }?.let {
                    modificationDate = it.item(0).textContent
                    Log.d("EmailReader", "ModificationDate: ${formatModificationDate(modificationDate)}")
                }

                var ActionCode = ""
                getElementsByTagName("ActionCode").takeIf { it.length > 0 }?.let {
                    ActionCode = it.item(0).textContent
                }

                var comment = ""
                when (ActionCode) {
                    "1" -> comment = "Принято в базу"
                    "3" -> comment = "ОФОРМЛЕНО"
                    "5" -> comment = "Сообщение с таможни"
                }

                var orgName = ""
                getElementsByTagName("OrgName").takeIf { it.length > 0 }?.let {
                    orgName = it.item(0).textContent
                }
                var DocCode = ""
                getElementsByTagName("DocCode").takeIf { it.length > 0 }?.let {
                    DocCode = it.item(0).textContent
                }

                var userName = ""
                getElementsByTagName("UserName").takeIf { it.length > 0 }?.let {
                    userName = it.item(0).textContent
                }

                var docInNum = ""
                getElementsByTagName("DocInNum").takeIf { it.length > 0 }?.let {
                    docInNum = it.item(0).textContent
                }

                var MRN = ""
                getElementsByTagName("ccd_54_02").takeIf { it.length > 0 }?.let {
                    MRN = it.item(0).textContent
                }
                var ccd_21_01 = ""
                getElementsByTagName("ccd_21_01").takeIf { it.length > 0 }?.let {
                    ccd_21_01 = it.item(0).textContent
                }
                var OrgCode = ""
                getElementsByTagName("OrgCode").takeIf { it.length > 0 }?.let {
                    OrgCode = it.item(0).textContent
                }
                var g_33_01 = ""
                getElementsByTagName("g_33_01").takeIf { it.length > 0 }?.let {
                    g_33_01 = it.item(0).textContent
                }

                if (g_33_01 != "") {
                    comment = "жди доставку \uD83D\uDCE6 $g_33_01"
                }

                if(ccd_21_01 != "") {
                    getElementsByTagName("ccd_29_02").takeIf { it.length > 0 }?.let {
                        modificationDate = it.item(0).textContent
                    }
                    var ccd_21_01 =""
                    getElementsByTagName("ccd_21_01").takeIf { it.length > 0 }?.let {
                        ccd_21_01 = it.item(0).textContent
                    }
                    comment = "Выехала 🚚 $ccd_21_01"
                    var ccd_07_01 =""
                    getElementsByTagName("ccd_07_01").takeIf { it.length > 0 }?.let {
                        ccd_07_01 = it.item(0).textContent
                    }
                    var ccd_07_02 =""
                    getElementsByTagName("ccd_07_02").takeIf { it.length > 0 }?.let {
                        ccd_07_02 = it.item(0).textContent
                    }
                    var ccd_07_03 =""
                    getElementsByTagName("ccd_07_03").takeIf { it.length > 0 }?.let {
                        ccd_07_03 = it.item(0).textContent
                    }
                    ccd_07_03 = ccd_07_03.padStart(6, '0')
                    docNumber = "Декларация: $ccd_07_01.$ccd_07_02.$ccd_07_03"
                }


                Log.d("EmailReader_Cont", "doc1Parsing:MRN $MRN")

                var flagAdd = true
                when (comment) {
                    "0" -> flagAdd = false
                }
                if(ActionCode != "3") {
                    when (OrgCode) {
                        "141000000" -> flagAdd = false
                    }
                }

                when (ActionCode) {
                    "0" -> flagAdd = false
                }


                if(flagAdd) {
                    currentEmailData = EmailData(
                        docNumber = docNumber,
                        modificationDate = formatModificationDate(modificationDate),
                        comment = comment,
                        subject = subject,
                        orgName = orgName,
                        userName = userName,
                        docInNum = docInNum,
                    )
                    addValueToResultList(resultList, currentEmailData)
                }
            }
        }
    }
    private fun doc2Parsing(
        zipInputStream: InputStream?,
        resultList: MutableList<EmailData>,
        subject: String
    ) {

        zipInputStream?.use { inputStream ->
            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(inputStream)

            // Найти и вывести значения нужных тегов
            with(doc) {
                var currentEmailData: EmailData? = null
                var docNumber = ""
                var modificationDate = ""
                var comment = ""
                var orgName = ""
                var userName = ""
                var docInNum = ""
                var phoneNumber = ""
                var Text = ""

                getElementsByTagName("MRN").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                }

                getElementsByTagName("ccd_registered").takeIf { it.length > 0 }?.let {
                    modificationDate = it.item(0).textContent

                }
                getElementsByTagName("ccd_trn_name").takeIf { it.length > 0 }?.let {
                    comment = "🚚 " + it.item(0).textContent

                }
                getElementsByTagName("ccd_01_01").takeIf { it.length > 0 }?.let {
                    comment += " -> " + it.item(0).textContent
                }
                getElementsByTagName("OrgName").takeIf { it.length > 0 }?.let {
                    orgName = it.item(0).textContent
                }
                getElementsByTagName("ccd_cl_name").takeIf { it.length > 0 }?.let {
                    userName = it.item(0).textContent
                }

                getElementsByTagName("Text").takeIf { it.length > 0 }?.let {
                    Text = it.item(0).textContent
                }


                if(Text != "") {
                    comment = Text
                    val resultListOld = getLastResultList(resultList)
                    if( resultListOld != null) {
                        modificationDate = resultListOld.modificationDate
                        docNumber = resultListOld.docNumber
                        orgName = resultListOld.orgName
                        userName = resultListOld.userName
                        docInNum = resultListOld.docInNum
                    }

                }

                getElementsByTagName("ccd_cl_tel").takeIf { it.length > 0 }?.let {
                    phoneNumber = it.item(0).textContent
                }

                orgName = phoneNumber ?: "" // Присваиваем значение phoneNumber переменной orgName, если phoneNumber не равен null, иначе пустую строку

                var flagAdd = true
                when (Text) {
                    "Досилку прийнято в базу даних" -> flagAdd = false

                }
                Log.d("EmailReader_Cont", "doc2Parsing: ")

                if(flagAdd) {
                    currentEmailData = EmailData(
                        docNumber = docNumber,
                        modificationDate = formatModificationDate(modificationDate),
                        comment = comment,
                        subject = subject,
                        orgName = orgName,
                        userName = userName,
                        docInNum = docInNum,
                    )
                    addValueToResultList(resultList, currentEmailData)
                }

            }
        }
    }
    private fun docListParsing(
            zipInputStream: InputStream?,
            resultList: MutableList<EmailData>,
            subject: String
        ) {
        zipInputStream?.use { inputStream ->
            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(inputStream)

            // Найти и вывести значения нужных тегов
            with(doc) {
                var currentEmailData: EmailData? = null
                var docNumber = ""
                var modificationDate = ""
                var comment = ""
                var orgName = ""
                var userName = ""
                var docInNum = ""
                var DocCode = ""

                getElementsByTagName("MRN").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                }
                getElementsByTagName("DocNumber").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                }
                getElementsByTagName("CreationDate").takeIf { it.length > 0 }?.let {
                    modificationDate = it.item(0).textContent

                }
                getElementsByTagName("Comment").takeIf { it.length > 0 }?.let {
                    comment = it.item(0).textContent
                }
                getElementsByTagName("ccd_01_01").takeIf { it.length > 0 }?.let {
                    comment += " -> " + it.item(0).textContent
                }
                getElementsByTagName("SenderName").takeIf { it.length > 0 }?.let {
                    orgName = it.item(0).textContent
                }
                getElementsByTagName("DocCode").takeIf { it.length > 0 }?.let {
                    DocCode = it.item(0).textContent
                }



                var flagAdd = true
                when (comment) {
                    "Підтвердження про отримання електронного повідомлення" -> flagAdd = false
                    "Протокол обробки електронного документа" -> flagAdd = false
                }
                when (DocCode) {
                    "61" -> flagAdd = false
                    "62" -> flagAdd = false
                }

                if(flagAdd) {
                    currentEmailData = EmailData(
                        docNumber = docNumber,
                        modificationDate = formatModificationDate(modificationDate),
                        comment = comment,
                        subject = subject,
                        orgName = orgName,
                        userName = userName,
                        docInNum = docInNum,
                    )
                    addValueToResultList(resultList, currentEmailData)
                }
            }
        }
    }

    private fun streamFile (
        zipInputStream: ZipArchiveInputStream
    ): ByteArrayInputStream {
        // Создаем ByteArrayOutputStream для записи содержимого файла
        val byteArrayOutputStream = ByteArrayOutputStream()
        var bytesRead: Int
        val buffer = ByteArray(1024)

        // Читаем содержимое файла entry в ByteArrayOutputStream
        while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        return  ByteArrayInputStream(byteArrayOutputStream.toByteArray())
    }


    private fun addValueToResultList(resultList: MutableList<EmailData>, currentEmailData: EmailData) {
        Log.d("EmailReader_Cont", "addValueToResultList: ")
        resultList.add(currentEmailData)
    }
    private fun getLastResultList(resultList: List<EmailData>): EmailData {
       return resultList.last()
    }


    @SuppressLint("SimpleDateFormat")
    private fun formatModificationDate(modificationDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss")
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
            outputFormat.format(inputFormat.parse(modificationDate)!!)
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("EmailReader", "Error formatting ModificationDate: ${e.message}")
            modificationDate // В случае ошибки, просто возвращаем исходную строку
        }
    }
}
