export class Login {
  constructor(
    public username: string,
    public password: string,
    public rememberMe: boolean,
    public captchaId: string,
    public captchaToken: string
  ) {}
}
