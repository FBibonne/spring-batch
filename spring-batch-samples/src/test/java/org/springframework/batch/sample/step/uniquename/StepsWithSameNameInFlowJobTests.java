package org.springframework.batch.sample.step.uniquename;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.sample.common.LogAdvice;
import org.springframework.batch.sample.jmx.StepExecutionApplicationEventAdvice;
import org.springframework.batch.support.IntArrayPropertyEditor;
import org.springframework.batch.support.SystemPropertyInitializer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.incrementer.AbstractColumnMaxValueIncrementer;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class StepsWithSameNameInFlowJobTests {

	private static String[] noms = { "step1", "step1", "step1", "step1" };
	
	private static String[] tags = { "S1", "S2", "S3", "S4" };

	private static StringBuilder sms = new StringBuilder();

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Test
	public void testFlow() throws Exception {

		jobLauncherTestUtils.launchJob();

		/*
		 * new DefaultJobParametersConverter().getJobParameters(PropertiesConverter.stringToProperties(
		 * "run.id(long)=1,parameter=true,run.date=20070122,input.file=classpath:data/fixedLengthImportJob/input/20070122.teststream.ImportTradeDataStep.txt"
		 * )));
		 */
		String resultat = sms.toString();
		System.out.println(resultat);
		assertTrue(resultat.equals("S1_step1ODDS2_step2") || resultat.equals("S1_step1EVENS3_step3S4_step4"));

		// assertEquals(5, after);
	}

	@Configuration
	public static class JavaConfigCommonsForTest {

		@Bean
		public JobLauncherTestUtils jobLauncherTestUtils(Job job) {
			JobLauncherTestUtils retour = new JobLauncherTestUtils();
			retour.setJob(job);
			return retour;
		}

		@Bean
		public DataSource datasource(@Value("${batch.jdbc.driver}") String driverClassName,
				@Value("${batch.jdbc.url}") String url, @Value("${batch.jdbc.user}") String username,
				@Value("${batch.jdbc.password}") String password, @Value("${batch.jdbc.pool.size}") int maxTotal,
				@Value("${batch.jdbc.validationQuery}") String validationQuery,
				@Value("${batch.jdbc.testWhileIdle}") boolean testWhileIdle) {
			BasicDataSource retour = new BasicDataSource();
			retour.setDriverClassName(driverClassName);
			retour.setUrl(url);
			retour.setUsername(username);
			retour.setPassword(password);
			retour.setMaxTotal(maxTotal);
			retour.setValidationQuery(validationQuery);
			retour.setTestWhileIdle(testWhileIdle);
			return retour;
		}

		@Bean
		public PlatformTransactionManager trasactionManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}

		@Bean
		public SystemPropertyInitializer environment() {
			SystemPropertyInitializer retour = new SystemPropertyInitializer();
			retour.setDefaultValue("hsql");
			retour.setKeyName("ENVIRONMENT");
			return retour;
		}

		@Bean
		@DependsOn("environment")
		public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
				@Value("classpath:batch-${ENVIRONMENT}.properties") Resource location) {
			PropertySourcesPlaceholderConfigurer retour = new PropertySourcesPlaceholderConfigurer();
			retour.setLocation(location);
			retour.setIgnoreUnresolvablePlaceholders(true);
			retour.setOrder(1);
			return retour;

		}

		@Bean
		public DataSourceInitializer dataSourceInitializer(DataSource dataSource,
				@Value("${batch.drop.script}") Resource dropScript,
				@Value("${batch.schema.script}") Resource schemaScript,
				@Value("${batch.business.schema.script}") Resource businessScript) {
			DataSourceInitializer retour = new DataSourceInitializer();
			retour.setDataSource(dataSource);
			ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(dropScript,
					schemaScript, businessScript);

			retour.setDatabasePopulator(resourceDatabasePopulator);
			return retour;

		}

		@Bean
		public AbstractColumnMaxValueIncrementer incrementerParent(DataSource dataSource,
				@Value("${batch.database.incrementer.class}") Class<? extends AbstractColumnMaxValueIncrementer> classe)
				throws InstantiationException, IllegalAccessException {
			AbstractColumnMaxValueIncrementer retour = classe.newInstance();
			retour.setDataSource(dataSource);
			retour.setIncrementerName("DUMMY");
			retour.setColumnName("ID");
			return retour;
		}

		@Bean
		public LobHandler lobHandler(@Value("${batch.lob.handler.class}") Class<? extends LobHandler> classe)
				throws InstantiationException, IllegalAccessException {
			return classe.newInstance();
		}

		@Bean
		public CustomEditorConfigurer customEditorConfigurer() {
			CustomEditorConfigurer retour = new CustomEditorConfigurer();
			int[] array = new int[0];
			retour.setCustomEditors(Map.of(array.getClass(), IntArrayPropertyEditor.class));
			return retour;
		}

		@Bean
		public JobRepositoryFactoryBean jobRepository(@Value("${batch.isolationlevel}") String isolationLevelForCreate,
				DataSource dataSource, PlatformTransactionManager transactionManager, LobHandler lobHandler) {
			JobRepositoryFactoryBean retour = new JobRepositoryFactoryBean();
			retour.setIsolationLevelForCreate(isolationLevelForCreate);
			retour.setDataSource(dataSource);
			retour.setTransactionManager(transactionManager);
			retour.setLobHandler(lobHandler);
			return retour;
		}

		@Bean(autowireCandidate = false)
		@Lazy(true)
		public MapJobRepositoryFactoryBean mapJobRepository() {
			return new MapJobRepositoryFactoryBean();
		}

		@Bean
		public SimpleJobOperator jobOperator(JobLauncher jobLauncher, JobExplorer jobExplorer,
				JobRepository jobRepository, ListableJobLocator jobRegistry) {
			SimpleJobOperator retour = new SimpleJobOperator();
			retour.setJobLauncher(jobLauncher);
			retour.setJobExplorer(jobExplorer);
			retour.setJobRepository(jobRepository);
			retour.setJobRegistry(jobRegistry);
			return retour;
		}

		@Bean
		public JobExplorerFactoryBean jobExplorer(DataSource dataSource) {
			JobExplorerFactoryBean retour = new JobExplorerFactoryBean();
			retour.setDataSource(dataSource);
			return retour;
		}

		@Bean
		public MapJobRegistry jobRegistry() {
			return new MapJobRegistry();
		}

		@Bean
		public JdbcTemplate jdbcTemplate(DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}

		@Bean
		public LogAdvice logAvdice() {
			return new LogAdvice();
		}

		@Bean
		public StepExecutionApplicationEventAdvice eventAdvice() {
			return new StepExecutionApplicationEventAdvice();
		}
		
		@Bean
		public JobLauncher jobLauncher(JobRepository jobRepository) {
			SimpleJobLauncher retour= new SimpleJobLauncher();
			retour.setJobRepository(jobRepository);
			return retour;
		}

	}

	@Configuration
	public static class JavaConfigFlowForTest {

		@Bean
		public Job flowjob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
			return (new JobBuilderFactory(jobRepository)).get("flowJob")
					.start(step1(jobRepository, transactionManager))
					.on("ODD").to(stepi(2, jobRepository, transactionManager))
					.from(step1(jobRepository, transactionManager)).on("EVEN").to(stepi(3,jobRepository, transactionManager))
					.from(stepi(3,jobRepository, transactionManager)).next(stepi(4,jobRepository, transactionManager))
					.end().build();
		}

		private Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
			return (new StepBuilderFactory(jobRepository, transactionManager)).get(noms[0]).tasklet(new Tasklet() {

				@Override
				public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
					String stepName = contribution.getStepExecution().getStepName();
					sms.append(tags[0] + "_" + stepName);
					long horloge = System.currentTimeMillis();
					String exitStatus = (horloge % 2 == 0) ? "EVEN" : "ODD";
					sms.append(exitStatus);
					contribution.setExitStatus(new ExitStatus(exitStatus));
					return RepeatStatus.FINISHED;
				}
			}).build();
		}

		public Step stepi(int i, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
			return (new StepBuilderFactory(jobRepository, transactionManager)).get(noms[i - 1]).tasklet(new Tasklet() {
				@Override
				public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
					String stepName = contribution.getStepExecution().getStepName();
					sms.append(tags[i - 1] + "_" + stepName);
					return RepeatStatus.FINISHED;
				}
			}).build();
		}

	}

}
