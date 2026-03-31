public class BcryptGen {
  public static void main(String[] args) {
    org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    System.out.println(enc.encode(args.length > 0 ? args[0] : "Admin123!"));
  }
}
