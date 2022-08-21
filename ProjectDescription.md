# PROJECT


## TEKNOLOJİLER
- Java
- Spring Boot
- MyBatis
- Kafka
- Spring Security
- MySQL
- Log4J
- Collect API
- Angular JS


## ENTITIES

1 - Sistemimiz çoklu bir bankacılık yönetim sistemidir. Sistemimizde bankalarımız olacaktır. Bankalarımızın
```
    {
        int id PRIMARY KEY AUTO_INCREMENT,
        string name NOT NULL UNIQUE
    }
```

şeklinde iki tane property'isi ve bunlara karşılık gelen kolonu olacaktır. Örnek olarak şu şekilde bankalar olacaktır
- 1 Akbank
- 2 KırmızıBank
- 3 AlBank
- 4 BootBank

vb...

2 - Sistemimizde kullanıcılar ve bunların yetkileri olacaktır. Kullanıcılarımızın
```
{
    int id PRIMARY KEY AUTO_INCREMENT,
    String username NOT NULL UNIQUE,
    String email NOT NULL UNIQUE,
    String password NOT NULL,
    boolean enabled DEFAULT true,
    String authorities
}
```

şeklinde property'leri ve kolonları olacaktır. Kolonları tanımlarken yanda yazdığım (NOT NULL, UNIQUE) gibi constraintsleri'de mutlaka eklemenizi istiyorum. authorities kolonu virgül ile ayrılmış şekilde(comma separated) yetkileri tutacaktır.

3 - Sistemimizdeki yetkiler

- CREATE_BANK
- ACTIVATE_DEACTIVATE_USER
- CREATE_ACCOUNT
- REMOVE_ACCOUNT

şeklinde olacaktır.

4 - Sistemimizde hesaplar olacaktır. Hesap dediğimiz 

- Unique(benzersiz) bir numaraya sahip(Hesap Numarası veya IBAN gibi düşünebilirsiniz) olan
- Para yatırılıp çekilebilen,
- Para transferi için kullanılabilen,
- Type'ı olan(Altın, Dolar, TL)
- Silinebilen

Gerçek hayattaki banka hesabı gibi yani.
Hesaplar bir bankaya ve ve kullanıcıya ait olmak zorundadır. Yani bir kullanıcın Akbank'ta bir hesabı olabilir. Sahipsiz veya bankası belli olmayan bir hesap olamaz. Yani hesap'ta
```
    {
        int id PRIMARY KEY AUTO_INCREMENT,
        user_id FOREING KEY(users.id),
        bank_id FOREIGN_KEY(banks.id),
        number int(10),
        enum type(TL,ALTIN,DOLAR)(String'de tutlabilir size bırakıyorum),
        double balance DEFAULT 0,
        timestamp creation_date,
        timestamp last_update_date,
        boolean is_deleted DEFAULT false
    }
```

## SECURITY

Sistemimizde güvenliği **JWT** token ile sağlayacağız. Ve userlarımızı database'de saklayıp, giriş yaparken database'den isteyeceğiz. (**Security Custom UserDetails Service**)

- Sisteminize manuel bir şekilde yetkileri şu şekilde olan userlar eklemenizi bekliyorum.
```
    id  username                password        enabled         authorities     
    1   admin                   123456(Encode edilmeli)          true            CREATE_BANK,CREATE_ACCOUNT,ACTIVATE_DEACTIVATE_USER,REMOVE_ACCOUNT
    2   account_manager         1234567(Encode edilmeli)         true            ACTIVATE_DEACTIVATE_USER,CREATE_ACCOUNT,REMOVE_ACCOUNT
```    

Burada admin kullanıcısı full yetkili olacaktır. **Account manager kullanıcısı'da sisteme kaydolan userları activate etme, hesap açma kapatma gibi işlemleri yapabilecektir.**

Buradak önemli nokta kullanıcı **şifrelerinin veritabanında açık bir şekilde tutulmamaları gerektiğidir**. Secuirty'e dair konfigürasyon yaptığımız sınıfımızda **PasswordEncoder bean'i kaydediyorduk hatırlayın.** Orada **NoOpPasswordEncoder** demiştik ödevlerimizde encode işlemi olmaması için, ama güvenlikten dolayı projemizde **BCryptPasswordEncoder** sınıfını password encoder beani olarak kaydetmenizi bekliyorum. Ve kullanıcı kayıt aşamasında; kullanıcının şifresini bu encoder ile encode'ladıktan sonra db'ye yazmanız gerekmektedir. Diğer işlemler(Loginde passworde bakma vb...) **AuthenticationManagerBuilder'a** encoderı düzgünce verirseniz otomatik olarak yapılacaktır zaten.

Bunlar dışında userlar sisteme **/register** şeklinde belirlediğiniz bir webservis aracılığıyla kayıt olacaktır. Sisteme bu şekilde kaydolan userlar

- CREATE_ACCOUNT

hakkına sahip olacaktır.

## SERVICES

Burada bankacılık işlemlerine dair sizden bir takım servisler yazmanızı bekliyorum. Buranın ana şartı REST'e uygunluk ve JSON formatı ile çalışmaktır. Zorlandığınız noktada REST'e dair ufak esnemeler yapabilirsiniz. (URL'e action yazmak mesela)

#### 1 - Create Bank 

Bu webservisimizde sisteme yeni bir banka eklenme işi yapılacaktır. Bu serivisimize sadece CREATE_BANK hakkına sahip kullanıcılar istek atabilmelidir.

Sisteme
```
    {
        "name" : "[bank_name]"
    }
```
şeklinde bir istek gelecektir. Burada
1.1 - Gelen name zaten kullanılıyor olabilir, bu durumda UYGUN HTTP kodu ile (Bunda ben söylemiş olayım diğerlerinde siz kendiniz araştırın, 422 Kodu) kullanıcıya
```
    {
        "success" : false,
        "message" : "Given name Already Used : [İstekte gelen name]"
    }
```
şeklinde bir hata dönmenizi bekliyorum.
1.2 Eğer isim kullanılmıyor ise bankayı yaratıp cevap olarak yaratılan banka nesnesini 201 HTTP status kodu ile dönmenizi istiyorum.
```
    {
        "success" : true,
        "message" : "Created Successfully",
        "bank" : {
            [Bankanın bilgileri]   
        }
    }
```

#### 2 - Register

Register kullanıcıların sisteme kayıt olabileceği webservisimiz olacaktır. URL'i **/register** olacaktır. Bu servis herkes tarafından erişilebilir olmalıdır.(**permitAll()**)
Servisimize 
```
    {
        "username" : "[username]",
        "email" : " [email]",
        "password" : "[password]"
    }
```
şeklinde istek gelecektir.

2.1 username ve email sistem genelinde unique(tekil) olmalıdır. Eğer bunlar halihazırda veritabanında kayıtlı olarak varsa cevap olarak uygun HTTP kodu ile
```
    {
        "success" : false,
        "message" : "Given username or email already Used : [İstekte gelen username veya email]"
    }
```
şeklinde cevap dönmenizi istiyorum.

2.2 Eğer bir sıkıntı yok ise **user'ı enabled'i false olacak şekilde database'e kaydetmenizi istiyorum.** Ve
```
    {
        "success" : true,
        "message" : "Created Successfully",
        "user" : {
            [User'ın bilgileri]   
        }
    }
```
şeklinde cevap dönmenizi bekliyorum.

#### 3 - Login

Bu webservisimiz kullanıcıların sisteme giriş yapabileceği webservistir. Kullanıcı **/auth** şeklinde bir URL'e username ve password'u ile bir **POST** isteği yapacak. Eğer bilgiler doğru ise kullanıcıya sonraki isteklerinde kullanabilmesi için JWT token üretilip dönülecektir.

```
    {
        "success" : true,
        "message" : "Logged-In Successfully",
        "token" : [Generated JWT Token]
    }
```

Bu serviste herkes tarafından authenticate gerektirmeden çağırılabilmelidir.
Kayıt olmasına rağmen enabled edilmemiş userlar sisteme girememelidir. Bu durumdaki bir user sisteme giriş yapmaya çalışırsa HTTP 403 status kodu ile cevap dönmenizi bekliyorum.


###### 4 - Enable, Disable User

Bu servisimiz'de **ACTIVATE_DEACTIVATE_USER** yetkisine sahip kullanıcılar; sistemdeki userları enable veya disable edebilecektir.

**/users/{id}** şeklinde bir adrese yapılacak
```
    {
        enabled: [true|false]
    }
```
şeklindeki bir istekle kullanıcı enable veya disable olabilecektir. Cevap olarak'ta uygun HTTP status kodu ile
```
    {
        "status" : "success",
        "message" : "User [Enabled Or Disabled]"
    }
```
dönmenizi bekliyorum.

**Disable edilen bir user'ı login aşamasında sisteme almamamız gerekmektedir.**

###### 5 - Create Account

Burada bir kullanıcı kendisine hesap açabilmektedir. Hesap yaratırken sadece

{
    "bank_id" : [bank_id]
    "type": [TL, Dolar, Altın]
}

şeklinde bankanın id'si ve hesabın tipi alınacaktır. Hesabın diğer kolonları

- balance = 0
- user_id = hesabı açmaya çalışan authenticate olmuş user'in id'si
- creation_date = işlemin yapıldığı anın timestampi
- last_update_date = creation_date ile aynı tarih
- is_deleted = false

şeklinde olacaktır yaratım aşamasında.

5.1 Burada eğer hesap tipi belirttiğimiz kümenin dışında olursa cevap olarak
```
    {
        "success" : false,
        "message" : "Invalid Account Type : [istekten gelen account type]"
    }
```
şeklinde uygun HTTP status kodu ile dönüş yapmanızı istiyorum.

5.2 Eğer bir sıkıntı yok ise account yaratılacaktır ve kullanıcıya UYGUN HTTP status kodu ile
```
    {
        "success" : true,
        "message" : "Account Created",
        "account" : {
            [yaratılan Account'un datası]
        }
    }
```
cevabı dönülecektir.


###### 6 - Account Detail

Bu servisimiz accountun detayını getiren servisimizdir. Herkes sadece kendi hesaplarından birisinin detayını görebilecektir. Burada authenticate olmuş user başka birine ait bir hesabın detayını görmek isterse
```
    {
        "message" : "Access Denied"
    }
```
şeklinde bir cevap dönmenizi istiyorum.

Eğer sıkıntı yok ise hesabı 
```
    {
        "account" : [account detail]    
    }
```
şeklinde cevap olarak dönmenizi istiyorum.

Burada önemli nokta döndüğünüz cevapta **Last-Modified** headerinin değerini hesabın **last_update_date** kolonunun değeri olarak set etmenizdir.

###### 7 - Remove Account

Bu servisimizde bir account silinebilecektir. Bu işlemi sadece **REMOVE_ACCOUNT** hakkına sahip olan kullanıcılar yapabilecektir. Burada
**Account database'den hard-delete bir şekilde silinmeyecektir**; bunun yerine account'un **is_deleted** kolonunun değeri true olarak güncellenecektir. Cevap olarakta
```
    {
        "success" : true,
        "message" : "Account Deleted"
    }
```
dönecektir.

###### 8 - Deposit

Deposit işlemi bir hesaba para yatırma işlemi olacaktır. Her kullanıcı SADECE kendi hesabına para yatırabilmelidir. Buraya **PATCH** methodu ile **/accounts/{id}** şeklinde 
```
    {
        "amount" : 100
    }
```
şeklinde bir istek gelecektir.

6.1 - Eğer hesap authenticate olmuş user bir hesabı değilse UYGUN HTTP status kodu ile (Unauthorized hatırlayın) 
```
    {
        "message" : "Access Denied"
    }
```
şeklinde bir cevap dönmenizi istiyorum.
6.2 Eğer bir sıkıntı yoksa account'un balance'i gelen istekteki amount kadar artırılacaktır. Ve kullanıcıya
```
    {
        "success" : true,
        "message" : "Deposit Successfully",
        "balance" : [new balance]
    }
```
şeklinde bir cevap dönmenizi istiyorum.

###### 9 - Transfer

Bu webservisimiz bir hesaptan diğer bir hesaba para transferi gerçekleştirecektir. Burada **/accounts/{senderAccountId}** şeklinde bir URL'e
```
    {
        "amount" : 100
        "receiverAccountId" : [receiver account_i]
    }
```
şeklinde bir istek gelecektir. Bu webservisimiz'in adımları şu şekilde olacaktır.

- Öncelikle herkes kendi hesabından başka hesaba transfer yapabilir. Yani senderAccountId authenticate olmuş user'ın bir account'u olmalıdır. Eğer başka birinin accountu ise
    ```
        {
            "message" : "Access Denied"
        }
    ```
    şeklinde cevap dönmenizi bekliyorum.

- Gönderici hesabın yeterli bakiyesi yoksa uygun HTTP status kodu ile
    ```
        {
            "message" : "Insufficient Balance
        }
    ```    
    şeklinde bir hata dönmenizi istiyorum.

- Transfer işlemi yapılırken hesapların type'ları farklı ise gerekli çevrimin düzgünce yapılıp alıcı hesapta bakiyenin buna göre artırılması gerekmektedir. Yani mesela ben bir dolar hesabından 10 doları bir TL hesabına transfer ediyor isem TL hesabının bakiyesi 10 doların TL karşılığı ne ise o kadar artırılmalıdır.
- Miktarlarda çevrim yaparken collectapi'nin bize sunduğu exchange api'sini kullanmanızı bekliyorum.
- Gönderici hesapla alıcı hesap başka bankalara ait ise;
    - Gönderen hesap TL ise 3 TL, dolar ise 1 dolar daha ekstradan eft ücreti almanızı bekliyorum. Gönderici hesap altın ise herhangi bir kesinti olmayacaktır. 

Buradaki önemli nokta; birden fazla database işlemi yapıldığı için işlemlerin transaction içinde yapılması gerektiğidir.(zorunludur)

## ADDITIONAL OPERATIONS

Arkadaşlar bir hesapta yapılan deposit, transfer gibi işlemlerin o hesabın last_update_date'ini işlemin yapıldığı an olarak güncellemesini bekliyorum.


## LOGGING

Arkadaşlar iki işlemde log tutmanızı istiyorum. 
- Deposit
- Transfer

Deposit işlemi için log mesajı
```
    [hesap numarası], [deposit miktarı] : deposited
```
Transfer işlemi için log mesajı
```
  [gönderilen miktar], [gönderen hesap numarası] to [gönderilen hesap numarası] : transferred
```

şeklinde olacaktır.

Burada süreç şu şekilde olacaktır.
- Kafka'da logs şeklinde bir topic'imiz olacaktır.
- Bu işlemlerden herhangi birisi gerçekleşirse KAFKA producer belirlenen formattaki mesajı KAFKA'ya gönderecektir.
- Daha sonra kafka'da dinlemede olan bir consumer kafka'dan log mesajını okuyacaktır.
- Ve consumer log mesajını **log4j** kullanarak /logs.txt adındaki bir dosyaya satır satır yazacaktır. **log4j** kullanmak zorundasınız arkadaşlar internette bolca örnek mevcut buna dair.




