# User Authentication System
It is very common for an online hosted service to be available to many people. However, if people simply use the service without providing any details or customizing the services for their needs, the service cannot provide a tailored solution to each of its customer.

> e.g., Consider a service to store/view content online. Something like a pastebin.
>> If this system doesn't store any User information such as content author, it is not possible for a user to manage a content after creation.
The system can still be used to share content but once content is created, the system has no way to connect the content with its author and hence content management feature cannot be made available to user.

A service can have a large number of users. Different services might need different level of user information. However, irrespective of the amount of user information, a service needs to have capabilities to allow a person to be register themselves (i.e., account creation), identify themselves with the system (i.e., login) and continue to work with the service (i.e., maintain an active session).

## Introduction
User Authentication System can be treated as a separate module which can be integrated into any service to achieve objectives of registration, login and active session management.

Any service which requires user information can request it from User Management System, which in turn can request user identity from User Authentication System. The User Authentication System needs a mechanism to validate the identity of the user (i.e., login) before it can create an active session (i.e., allow subsequent use of service without login). It should also be noted that the identity of only those users can be validated who are already registered with the system thus User Authentication System also needs to take care of user registration process.

This system needs to be secure to not allow anyone else to impersonate a different user. It should also prevent everyone (including owners of service) from viewing users credential (i.e., password). The system should be resistant to data breaches (i.e., even if someone gains access to database, misuse of the data should be minimal).

This system can be broken down into following components to make it easy to understand different challenges:
- Front-end (UI client with which a user interacts with the system)
- Network (External and internal network over which data transfer takes place)
- Back-end (The part of system which receives data from front-end, applies logic to data and interacts with database)
- Database (A database for storing user data)

As this is a very sensitive system, we shall mostly be focused on creating a secure system. We might also explore performance and scaling aspects, wherever necessary.

### Creating a new account
A user gets a form to fill and submit for creation of an account. The form contains some mandantory fields such as 
- email
- password
and can contain other optional fields such as
- name
- phone
- country

The form is usually rendered on a front-end, to give a more pleasing experience to user as well as to make a more secure system compared to interacting directly with an API. Once user submits the form, it is sent to back-end over network. The back-end might apply some logic to transform the data and finally store it in a database.

![User Account Creation with data sent on http](UAM1.jpg)

Let's consider that system exposes following APIs:

- Create Account
```
POST /account
{
    email:{value},
    password:{value},
}
```

- Login
```
POST /login
{
    email:{value},
    password:{value},
}
```

#### Scenario: The network is insecure
If the form data is sent on an insecure network such as HTTP instead of HTTPS, it might be possible for a Man-in-the-middle to view data filled by user. The data sent over HTTP is plain text whereas data sent over HTTPS is encrypted (usually using RSA).

When a user submits a form, the form-data is broken down into several packets (think of it as small fragments of form) on the front-end system and transferred via network to back-end system. It is no surprise that this data channels through various different systems in the network before finally arriving at the destination. Since the data transferred is in plain-text, any intermediate system can combine the packets and view the form and thus obtain user's credential (i.e., email and password) and might be able to impersonate the original user. (Such an attack is called Man-in-the-middle and usually the malicious device in the network is a public WIFI router).

It is not possible to avoid a malicious device in the network, even impossible since it's usually the router serving WIFI to which the user's device is directly connected. So, there is a need of a mechanism to secretly pass the data which can only be viewed by the back-end. The idea is to encrypt the data at the front-end before sending it on network so any intermittent devices can only view encrypted data and the back-end must be able to decrypt the data and thus obtain actual data filled at front-end. The RSA algorithm is a popular mechanism to create a encryption which is not easy to decrypt (except for actual intended users) and HTTPS network transfer utilizes it.

> Solution: Use HTTPS

![User Account Creation with data sent on https](UAM2.jpg)

#### Scenario: User has kept same passwords on multiple platform and someone has access to read data from database
The database engineer on the project would probably have administrator access on the database (Access is required to manage database. There are ways to minimize the adverserial impact due to such access but neverthless, system should be made more secure) or it could even be a hacker who has got access through illegitimate means. Such a person can view stored credential and can use it for impersonating someone or even to login to other services (If the user has kept same password for different sites. Hence, a user should always try to keep unique passwords!).

Instead of storing passwords as-is, system should store a different value so that even if someone gets acccess to stored credentials, the impact is restricted to single service (e.g., When Y company database leaks credential data, it can only be used to login at Y. Using this data to login at Z wouldn't work). A way to achieve this is by hashing the password.

It should be noted that system is converting the password in to a form which cannot be used to recover the original password through hashing. Thus a perpetrator only gains access to limited data. Applying the same hash function on password would give the same value and hence system can still validate indentity of user. However, the person with access to hashed value cannot recover orignal password and hence shouldn't be able to login (Atleast not without exposing another API for login, which only takes hashed valued instead of password).

It should be noted that during data transmission, an encryption mechanism was used as there was a need to recover the original value (by decryption). However, since the system doesn't need to know exact password to be able to perform the match, hashing is more favourable here.

> Solution: Hash the value received at backend before storing in database

![User Account Creation with data hashed after transfer on https](UAM3.jpg)

#### Scenario: User has kept the same passwords on multiple platform and someone has access to debug or log data recieved at back-end
This is somewhat a similar scenario as earlier. However, the scenario is less likely as debugging access or logging sensitive information is not considered a good practice (There can be some service providers which might do it, hence a user should keep unqiue passwords by themselves.).

The hashing logic can be applied in this case too! The hashing must be done before anyone else can access it. Front-end is the best place to do it. Also, this is the reason to enforce account creation via a UI (so that hashing and other logic can be applied) instead of directly using APIs (which would send the data to back-end without any processing).

So, the password is hashed on the client-side and the hashed password is sent to server. This hashed password is hashed again before storing it in database. Somehow, if someone even gets access to view the data received at back-end, all they'd get is a hashed value. This is still helpful as the pattern used by user in original password is now lost after hashing it. A perpetrator with hashed password might try to login by sending the hashed value directly in API (to avoid rehashing) instead of logging using form (We shall look into this in more detail in login section).

> Solution: Hash password at front-end

![User Account Creation with data hashed before transfer on https](UAM4.jpg)

#### Scenario: Multiple user has kept the same password
The system is storing the data after hashing it. However, hashing same values produces same hash! (This is how system performs the match).

In case of a database breach, if original password of any hash is computed, it'd impact all users having same password. The system can add a random value (the random value is called salt) to the password (actually, it is the hashed password sent from client) before rehashing this combination and storing it in database. The salt needs to be stored in a different database to make things difficult for hacker.

> Solution: Add a random string to hashed password

![User Account Creation with salt added to password](UAM5.jpg)

It is still possible that a hacker has access to both salt and the password. To make the system even more secure, an additional string (called pepper) is added to combination of hashed password and salt. Pepper doesn't have to be unique. It simply adds another layer of security to make things difficult for perpetrator.

![User Account Creation with salt and pepper added to password](UAM6.jpg)

### Logging in to an account
The system provides user with a login form. User inputs their credentials such as email and password and submits the form.

The form processes the input from password to hash it using same hash function used during account creation. The hashed password and email data is sent to back-end, where the salt corresponding to email-id is retrieved from the database. The string corresponding to pepper is also retrieved (It can be from a database, an API or even a constant variable in code). Salt and pepper is added to hashed password and rehashed and compared with value stored in database to validate users credential. 

After successful validation, user should be able to use any of authorized service. As of now, the system is not keeping a track of logged in users, it is simply validating details provided by user. Due to this limitation, a user needs to provide their credential in each request. Each API request would first perform the validation and then do actual processing requested by user.

#### Scenario: User makes multiple API calls (Maintaining an active session)
If we consider a process of making an online purchase, it involves several steps. Each step would be an API call for the user. As mentioned earlier, each API call needs to contain the users credential. It would be a very bad user experience if the user need to enter their credential each time. It can be simplified by storing credential locally in browser cookie and re-use it in each API request.

> Solution: Store the credential in cookie

Storing the credential in cookie is not a good idea because a perpetrator gaining access to the cookie might be able to view stored credentials and use it for logging it. Instead of storing credentials in plain-text, a server can issue can token upon successful validation. This token can be stored in the cookie and re-used. Moreover, unlike password which don't expire for a long time, a token can have limited life-span.

> Solution: Create a session token and store it in cookie.

![Login process with token](UAM2.jpg)

#### Scenario: A perpetrator has gained access to password/hashed password
In such scenarios, additional information related to user can help to determine identity. e.g., A user based in India created an account. However, the login attempt is made from USA. The location data, in itself might not be a sufficient means to validate identity.

A more sophisticated approach could be to allow users to create an allow-list. The allow-list can store MAC-ID of users devices, and thus a valid login attempt must always come from one of devices in allow-list.

Another layer of security can be added by sending a One-Time-Password (OTP) on registered email or phone whenever a login attempt is made from abnormal location/device/etc.

> Solution: 
> - Use additional details such as location to identify abnormal requests
> - Use allow-list of devices
> - Use of multi-factor authentication mechanisms like OTP verification using email or phone

#### Scenario: A perpetrator is trying to guess an accounts password
The system should not allow anyone to make unlimited guesses for password. A login attempt against an account should be rate-limited (i.e., only a few request can be made in a certain time frame).

This might prove to be detrimental for the end-user as someone can prevent a valid user from logging in their account. Two-factor mechanism can be used for validating a user in such cases. A perpetrator trying to guess credential would require a lot of guesses which would be rate-limited. Whereas, upon successful credential validation from a user, multi-factor method adds additional security.

> Solution: Rate-limit login requests

#### Scenario: A perpetrator has setup an identical website hoping for users to fall prey to phishing attempt
The authenticity of a website can be verified easily from its certificate. However, a normal user might not be educated enough to validate authenticity using certification. Most browsers often check for certificate validation and warns a user.

Along with certificate validation, another method of displaying a stored secret message can be used. In this method, a user sets a secret message during account creation step. During login, the user only enters emailId or any kind of account identifier. The system should display the secret message stored by user during account creation. Only after validating the secure message, the user should proceed to enter password and complete the login process.

While the above mechanisms might not be sufficient, it would still be effective in reducing the impact especially when combined with rate-limiting approach.


### Additional thoughts
- Instead of creating one's own User Authentication System, the service can also use trusted authentication systems (OAuth).
- A certificate based method can also be utilized, thus avoiding need for user to enter credentials at all.

As much crucial it is to secure a User Account, it is also equally important to simplify the process for actual user. Adding too many layers of secuity might prove to be a nightmare for actual users to be able to perform a successful login.



