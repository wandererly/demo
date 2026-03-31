public class BcryptCheck {
  public static void main(String[] args) {
    String raw = args.length>0?args[0]:"Admin123!";
    String hash = args.length>1?args[1]:"";
    org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    System.out.println(enc.matches(raw, hash));
  }
}
